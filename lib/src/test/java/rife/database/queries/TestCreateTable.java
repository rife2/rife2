/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import rife.database.DbQueryManager;
import rife.database.DbTransactionUserWithoutResult;
import rife.database.exceptions.DatabaseException;
import rife.tools.InnerClassException;

public abstract class TestCreateTable extends TestQuery {
    public void execute(CreateTable query) {
        DbQueryManager manager = new DbQueryManager(query.getDatasource());

        Execution execution = new Execution(query, manager);
        if (query.isTemporary()) {
            manager.inTransaction(execution);
        } else {
            execution.useTransactionWithoutResult();
        }
    }

    class Execution extends DbTransactionUserWithoutResult {
        private CreateTable query_ = null;
        private DbQueryManager manager_ = null;

        public Execution(CreateTable query, DbQueryManager manager) {
            query_ = query;
            manager_ = manager;
        }

        public void useTransactionWithoutResult()
        throws InnerClassException {
            CreateTable foreign_table = new CreateTable(query_.getDatasource());
            try {
                if (query_.getForeignKeys().size() > 0) {
                    foreign_table.table("foreigntable")
                        .column("foreignIntColumn", int.class)
                        .column("foreignStringColumn", String.class, 50)
                        .unique("foreignIntColumn")
                        .primaryKey(new String[]{"foreignIntColumn", "foreignStringColumn"})
                        .temporary(query_.isTemporary());

                    manager_.executeUpdate(foreign_table);
                }

                // try to execute the table creation
                manager_.executeUpdate(query_);

//				String driver = mManager.getDatasource().getAliasedDriver();
//				System.out.println("\n");
//				System.out.println(driver);
//				System.out.println(mQuery.getSql());
//
//				DatabaseMetaData	meta_data = mManager.getConnection().getMetaData();
//				try
//				{
//					String table_name = mQuery.getTable();
//					if (driver.equals("oracle.jdbc.driver.OracleDriver") ||
//						driver.equals("org.hsqldb.jdbcDriver") ||
//						driver.equals("org.apache.derby.jdbc.EmbeddedDriver"))
//					{
//						table_name = table_name.toUpperCase();
//					}
//
//					ArrayList<String> primary_keys = new ArrayList<String>();
//
//					ResultSet primary_keys_rs = meta_data.getPrimaryKeys(null, null, table_name);
//					while (primary_keys_rs.next())
//					{
//						primary_keys.add(primary_keys_rs.getString("COLUMN_NAME"));
//					}
//
//					System.out.println("COLUMNS");
//					ResultSet columns_rs = meta_data.getColumns(null, null, table_name, "%");
//					while (columns_rs.next())
//					{
//						String column_name = columns_rs.getString("COLUMN_NAME");
//						int data_type = columns_rs.getInt("DATA_TYPE");
//						String type_name = columns_rs.getString("TYPE_NAME");
//						int column_size = columns_rs.getInt("COLUMN_SIZE");
//						int column_decimal_digits = columns_rs.getInt("DECIMAL_DIGITS");
//						String column_default = columns_rs.getString("COLUMN_DEF");
//						boolean column_primarykey = primary_keys.contains(column_name);
//						boolean column_nullable = true;
//						if(columns_rs.getString("IS_NULLABLE").equals("NO"))
//						{
//							column_nullable = false;
//						}
//						String remarks = columns_rs.getString("REMARKS");
//						System.out.println(column_name+"; "+data_type+"; "+type_name+"; "+column_size+"; "+column_decimal_digits+"; "+column_default+"; "+column_primarykey+"; "+column_nullable+"; "+remarks);
//					}
//
//					System.out.println("INDICES");
//					ResultSet index_rs = meta_data.getIndexInfo(null, null, table_name, false, false);
//					while (index_rs.next())
//					{
//						String column_name = index_rs.getString("COLUMN_NAME");
//						boolean	non_unique = index_rs.getBoolean("NON_UNIQUE");
//						String index_qualifier = index_rs.getString("INDEX_QUALIFIER");
//						String index_name = index_rs.getString("INDEX_NAME");
//						short type = index_rs.getShort("TYPE");
//						String asc_or_desc = index_rs.getString("ASC_OR_DESC");
//						String filter_condition = index_rs.getString("FILTER_CONDITION");
//						System.out.println(column_name+"; "+non_unique+"; "+index_qualifier+"; "+index_name+"; "+type+"; "+asc_or_desc+"; "+filter_condition);
//					}
//
//					System.out.println("FOREIGN KEYS");
//					ResultSet imported_keys_rs = meta_data.getImportedKeys(null, null, table_name);
//					while (imported_keys_rs.next())
//					{
//						String fkcolumn_name = imported_keys_rs.getString("FKCOLUMN_NAME");
//						String pktable_name = imported_keys_rs.getString("PKTABLE_NAME");
//						String pkcolumn_name = imported_keys_rs.getString("PKCOLUMN_NAME");
//						short update_rule = imported_keys_rs.getShort("UPDATE_RULE");
//						short delete_rule = imported_keys_rs.getShort("DELETE_RULE");
//						String fk_name = imported_keys_rs.getString("FK_NAME");
//						String pk_name = imported_keys_rs.getString("PK_NAME");
//						System.out.println(fkcolumn_name+"; "+pktable_name+"; "+pkcolumn_name+"; "+update_rule+"; "+delete_rule+"; "+fk_name+"; "+pk_name);
//					}
//				}
//				catch (SQLException e)
//				{
//					e.printStackTrace();
//				}

                // it was successful, remove the table again
                manager_.executeUpdate(new DropTable(query_.getDatasource()).table(query_.getTable()));
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            } finally {
                // clean up foreign key table
                try {
                    if (query_.getForeignKeys().size() > 0) {
                        manager_.executeUpdate(new DropTable(query_.getDatasource()).table(foreign_table.getTable()));
                    }
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
