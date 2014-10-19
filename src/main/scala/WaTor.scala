import scala.util.Random

abstract class Animal(var age: Int, val reproduceTime: Int) {
  var lastStep: Int = -1

  def duplicate: Animal
  def isFish: Boolean
  def isShark: Boolean

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

  private def newShark = new Shark(0, shark._2, shark._3)
  private def newFish = new Fish(0, fish._2)

  def fishReprTime = fish._2
  def sharkReprTime = shark._2
  def sharkEnergy = shark._3

  private val (height, width) = wh
  private val (fishes, sharks) = (fish._1, shark._1)

  val ocean: Array[Array[Animal]] = create(fishes, sharks)

  private def create(fishCount: Int, sharkCount: Int): Array[Array[Animal]] = {
    val rand = scala.util.Random
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

  private def countAll(ocean: Array[Array[Animal]])(f: Animal => Boolean): Int = {
    if(ocean.isEmpty) 0
    else ocean.head.count(f) + countAll(ocean.tail)(f)
  }

  def countFishes: Int = countAll(ocean)(a => null != a && a.isFish)

  def countSharks: Int = countAll(ocean)(a => null != a && a.isShark)

  def forward(step: Int): Unit = {
    0.until(height).foreach(i => 0.until(width).foreach(j => if(null != ocean(i)(j) && ocean(i)(j).lastStep < step) oneStep(i, j, step)))
  }

  def oneStep(i: Int, j: Int, step: Int): Unit = {
    val possible: List[(Int, Int)] = List(
      (i-1, j-1), (i-1, j), (i-1, j+1),
      (i, j-1), (i, j+1), (i+1, j-1),
      (i+1, j), (i+1, j+1)
    ).filter(p => 0 <= p._1 && p._1 < height && 0 <= p._2 && p._2 < width)
    val empty: List[(Int, Int)] = possible.filter(p => null == ocean(p._1)(p._2))
    val fishes: List[(Int, Int)] = possible.filter(p => null != ocean(p._1)(p._2) && ocean(p._1)(p._2).isFish)
    ocean(i)(j).action((i, j), ocean, empty, fishes)
  }
}