package dev.unnm3d.rediseconomy.transaction;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    public UUID sender = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public long timestamp = 0;
    public UUID receiver = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public double amount = 0;
    public String currencyName = "";
    public String reason = "Unknown";

    public static Transaction fromString(String s) {
        String[] parts = s.split(";");
        if (parts.length != 6) return null;
        return new Transaction(
                UUID.fromString(parts[0]),
                Long.parseLong(parts[1]),
                UUID.fromString(parts[2]),
                Double.parseDouble(parts[3]),
                parts[4],
                parts[5]);
    }

    @Override
    public String toString() {
        return sender + ";" + timestamp + ";" + receiver + ";" + amount + ";" + currencyName + ";" + reason;
    }
}
