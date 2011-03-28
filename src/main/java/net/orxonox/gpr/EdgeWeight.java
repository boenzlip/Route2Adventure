package net.orxonox.gpr;

/**
 * Minimalistic edge weight.
 * 
 */
public class EdgeWeight implements IEdgeWeight {

  private double weight;

  public EdgeWeight(double weight) {
    this.weight = weight;
  }

  /*
   * @see net.orxonox.gpr.IEdgeWeight#getWeight()
   */
  public double getWeight() {
    return weight;
  }

}
