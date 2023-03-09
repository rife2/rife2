package rife.bld;

public interface CliCommands {
    boolean create()
    throws Exception;

    boolean compile()
    throws Exception;

    boolean encrypt()
    throws Exception;

    boolean precompile()
    throws Exception;

    boolean help()
    throws Exception;
}
