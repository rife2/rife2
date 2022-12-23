package rife.database;

@FunctionalInterface
public interface TransactionUser<ResultType> {
    ResultType useTransaction();
}