package rife.bld;

public interface CliCommands {
    boolean create()
    throws Exception;

    boolean download()
    throws Exception;

    boolean compile()
    throws Exception;

    boolean clean()
    throws Exception;

    boolean run()
    throws Exception;

    boolean jar()
    throws Exception;

    boolean war()
    throws Exception;

    boolean encrypt()
    throws Exception;

    boolean precompile()
    throws Exception;

    boolean help()
    throws Exception;
}
