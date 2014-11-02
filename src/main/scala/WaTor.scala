import scala.util.Random

abstract class Animal(var age: Int, val reproduceTime: Int) {
  var lastStep: Int = -1

  def duplicate: Animal
  def isFish: Boolean
  def isShark: Boolean
  private val SHARK_ENERGY_FROM_ONE_FISH = 3

  /**
   * makes one step for one element of ocean
   * @param curr - current position (column)
   * @param ocean - all world
   * @param empty - all empty columns position current position
   * @param fishes - all position with fishes around current position
   */
  def action(curr: (Int, Int), ocean: Array[Array[Animal]], empty: List[(Int, Int)], fishes: List[(Int, Int)]): Unit = {
    this match {
      case shark: Shark =>
        shark.lastStep += 1
        if(shark.energy == 0){
          ocean(curr._1)(curr._2) = null
        } else {
          if(fishes.nonEmpty || empty.nonEmpty){
            if(shark.age == shark.reproduceTime){
              shark.age = 0
              ocean(curr._1)(curr._2) = shark.duplicate
            } else {
              shark.age += 1
              ocean(curr._1)(curr._2) = null
            }
            if(fishes.nonEmpty){
              shark.energy += (SHARK_ENERGY_FROM_ONE_FISH - 1)
              val head: (Int, Int) = Random.shuffle(fishes).head
              ocean(head._1)(head._2) = shark
            } else {
              shark.energy -= 1
              val head: (Int, Int) = Random.shuffle(empty).head
              ocean(head._1)(head._2) = shark
            }
          } else {
            shark.energy -= 1
            if(shark.age == shark.reproduceTime){
              shark.age = 0
            } else {
              shark.age += 1
            }
          }
        }
      case fish: Fish =>
        fish.lastStep += 1
        if(empty.nonEmpty){
          if(fish.age == fish.reproduceTime){
            fish.age = 0
            ocean(curr._1)(curr._2) = fish.duplicate
          } else {
            fish.age += 1
            ocean(curr._1)(curr._2) = null
          }
          val head: (Int, Int) = Random.shuffle(empty).head
          ocean(head._1)(head._2) = fish
        } else {
          if(fish.age == fish.reproduceTime){
            fish.age = 0
          } else {
            fish.age += 1
          }
        }
    }
  }
}

class Shark(age: Int, reproduceTime: Int, var energy: Int) extends Animal(age, reproduceTime) {
  override def duplicate: Animal = new Shark(0, reproduceTime, 1)
  override def isFish: Boolean = false
  override def isShark: Boolean = true
}

class Fish(age: Int, reproduceTime: Int) extends Animal(age, reproduceTime) {
  override def duplicate: Animal = new Fish(0, reproduceTime)
  override def isFish: Boolean = true
  override def isShark: Boolean = false
}

class World(wh: (Int, Int), fish: (Int, Int), shark: (Int, Int, Int)) {

  private val rand = scala.util.Random
  private def newShark = new Shark(0, shark._2, rand.nextInt(shark._3-1)+1)
  private def newFish = new Fish(0, fish._2)

  private val (height, width) = wh
  private val (fishes, sharks) = (fish._1, shark._1)

  val ocean: Array[Array[Animal]] = create(fishes, sharks)

  /**
   * initialize model
   * @param fishCount - count of fishes
   * @param sharkCount - count of sharks
   * @return new ocean with animals
   */
  private def create(fishCount: Int, sharkCount: Int): Array[Array[Animal]] = {
    def randomPositions(coordinates: Set[(Int, Int)]): Set[(Int, Int)] = {
      if (coordinates.size == (fishCount + sharkCount)) coordinates
      else randomPositions(coordinates + ((rand.nextInt(height), rand.nextInt(width))))
    }
    def modifyOcean(ocean: Array[Array[Animal]], positions: Set[(Int, Int)], counter: Int): Array[Array[Animal]] = {
      if (positions.isEmpty) ocean
      else if (counter < fishCount) {
        ocean(positions.head._1)(positions.head._2) = newFish
        modifyOcean(ocean, positions.tail, counter+1)
      }
      else {
        ocean(positions.head._1)(positions.head._2) = newShark
        modifyOcean(ocean, positions.tail, counter+1)
      }
    }
    modifyOcean(Array.ofDim(height, width), randomPositions(Set()), 0)
  }

  /**
   * counts all animals of some type
   * @param ocean - all world where exist some animals
   * @param f - function which returns true if this type of animal need to be counted
   */
  private def countAll(ocean: Array[Array[Animal]])(f: Animal => Boolean): Int = {
    if(ocean.isEmpty) 0
    else ocean.head.count(f) + countAll(ocean.tail)(f)
  }

  /**
   * counts all exist fishes
   */
  def countFishes: Int = countAll(ocean)(a => null != a && a.isFish)

  /**
   * counts all exist sharks
   */
  def countSharks: Int = countAll(ocean)(a => null != a && a.isShark)

  /**
   * makes one step for all elements of the ocean
   * @param step - global number of step
   */
  def forward(step: Int): Unit = {
    0.until(height).foreach(i => 0.until(width).foreach(j => if(null != ocean(i)(j) && ocean(i)(j).lastStep < step) oneStep(i, j, step)))
  }

  /**
   * makes one step for one element of matrix
   * @param i - row
   * @param j - column
   * @param step - global number of step
   */
  def oneStep(i: Int, j: Int, step: Int): Unit = {
    val possible: List[(Int, Int)] = List(
      (i-1, j-1), (i-1, j), (i-1, j+1),
      (i, j-1), (i, j+1), (i+1, j-1),
      (i+1, j), (i+1, j+1)
    ).filter(p => 0 <= p._1 && p._1 < height && 0 <= p._2 && p._2 < width)
    /*val possible: List[(Int, Int)] = List(
      (i-1, j), (i, j-1), (i, j+1), (i+1, j)
    ).filter(p => 0 <= p._1 && p._1 < height && 0 <= p._2 && p._2 < width)*/
    val empty: List[(Int, Int)] = possible.filter(p => null == ocean(p._1)(p._2))
    val fishes: List[(Int, Int)] = possible.filter(p => null != ocean(p._1)(p._2) && ocean(p._1)(p._2).isFish)
    ocean(i)(j).action((i, j), ocean, empty, fishes)
  }
}