package dev.unnm3d.rediseconomy.transaction;

import dev.unnm3d.rediseconomy.RedisEconomyPlugin;
import dev.unnm3d.rediseconomy.currency.CurrenciesManager;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static dev.unnm3d.rediseconomy.redis.RedisKeys.TRANSACTIONS;

@AllArgsConstructor
public class EconomyExchange {

    private final CurrenciesManager currenciesManager;

    public CompletableFuture<Transaction[]> getTransactions(UUID player) {
        return currenciesManager.getRedisManager().getConnection(connection -> {
            connection.setTimeout(Duration.ofMillis(1000));
            return connection.async().hget(TRANSACTIONS.toString(), player.toString()).thenApply(this::getTransactionsFromSerialized).toCompletableFuture();
        });
    }

    public void saveTransaction(RedisAsyncCommands<String, String> client, UUID sender, UUID target, double amount) {
        long init = System.currentTimeMillis();
        client.hmget(TRANSACTIONS.toString(), sender.toString(), target.toString()).thenApply(lista -> {
            if (RedisEconomyPlugin.settings().DEBUG) {
                Bukkit.getLogger().info("03 Retrieve transactions from redis... next 02");
            }
            //Deserialize
            Transaction[] senderTransactions = getTransactionsFromSerialized(lista.get(0).isEmpty() ? null : lista.get(0).getValue());
            Transaction[] receiverTransactions = getTransactionsFromSerialized(lista.get(1).isEmpty() ? null : lista.get(1).getValue());

            //Add a space into arrays and delete the oldest transaction
            senderTransactions = updateArraySpace(senderTransactions);
            receiverTransactions = updateArraySpace(receiverTransactions);

            //Add the new transaction
            senderTransactions[senderTransactions.length - 1] = new Transaction(sender, System.currentTimeMillis(), target, -amount, "vault", "Payment");
            receiverTransactions[receiverTransactions.length - 1] = new Transaction(sender, System.currentTimeMillis(), target, amount, "vault", "Payment");

            //Serialize transactions
            Map<String, String> map = Map.of(sender.toString(), serializeTransactions(senderTransactions), target.toString(), serializeTransactions(receiverTransactions));

            //Save transactions into redis
            return currenciesManager.getRedisManager().getConnection(connection -> {
                connection.async().hset(TRANSACTIONS.toString(), map).thenAccept(response -> {
                    if (RedisEconomyPlugin.settings().DEBUG) {
                        Bukkit.getLogger().info("03b Transaction for " + sender + " saved in " + (System.currentTimeMillis() - init) + " ms with result " + response + " !");
                    }
                });
                return null;
            });
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            return null;
        });

    }

    private Transaction[] updateArraySpace(Transaction[] transactions) {
        final int transactionMaxSize = RedisEconomyPlugin.settings().TRANSACTIONS_RETAINED;
        Transaction[] newTransactions;
        if (transactions.length > transactionMaxSize - 1) {
            newTransactions = new Transaction[transactionMaxSize];
            System.arraycopy(transactions, 1, newTransactions, 0, transactionMaxSize - 1);
        } else {
            newTransactions = new Transaction[transactions.length + 1];
            System.arraycopy(transactions, 0, newTransactions, 0, transactions.length);
        }
        return newTransactions;
    }

    private Transaction[] getTransactionsFromSerialized(String serialized) {
        if (serialized == null)
            return new Transaction[0];
        String[] split = serialized.split("/");
        Transaction[] transactions = new Transaction[split.length];
        for (int i = 0; i < split.length; i++) {
            try {
                transactions[i] = Transaction.fromString(split[i]);
            } catch (Exception e) {
                transactions[i] = null;
            }
        }
        return transactions;

    }

    public String serializeTransactions(Transaction[] transactions) {
        StringBuilder builder = new StringBuilder();
        for (Transaction transaction : transactions) {
            builder.append(transaction.toString()).append("/");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }


}
