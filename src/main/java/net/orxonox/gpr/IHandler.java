package net.orxonox.gpr;

/**
 * Interface for all services.
 * 
 */
public interface IHandler<T> {

  /**
   * Initializes the service.
   */
  void init();

  /**
   * Process data type specific.
   * 
   * @param inputData
   */
  void handle(T inputData);

  /**
   * Deinitialize the service. Frees all resources.
   */
  void teardown();
}
