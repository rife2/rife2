package rife.database;

@FunctionalInterface
public interface TransactionUserWithoutResult {
    void useTransaction();
}