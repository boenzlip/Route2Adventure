package net.orxonox.gpr.store;

/**
 * Store pattern, generic interface for a store that offers output data
 * identified by input data descriptor.
 * 
 * @param <IN>
 *          type of data descriptor.
 * @param <OUT>
 *          type of output data.
 */
public interface IStore<IN, OUT> {

  /**
   * Initialize the store.
   */
  void init();

  /**
   * Deinitialize the store, release resources.
   */
  void teardown();

  /**
   * Process a request to aquire data.
   * 
   * @param descriptor
   *          data descriptor.
   * @return data.
   */
  OUT aquire(IN descriptor);

}
