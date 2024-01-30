package example

import org.scalajs.dom
import com.raquo.laminar.api.L.{*, given}

val ballDiameter = 32
val blockCount = 1024 / ballDiameter
val blockSize = ballDiameter
val ballSpeed = 8

@main
def helloWorld(): Unit =
  val appContainer: dom.Element = dom.document.querySelector("#app")
  val tickStream = EventStream.periodic(4)
  val ballVar = Var((64, 128, ballSpeed, ballSpeed))
  val ballVar2 = Var((512 + 64, 512 + 128, ballSpeed, ballSpeed))
  val blocksVar = Var(Array.fill(blockCount, blockCount)(0))
  // // Initially, right half of the blocks are filled
  // for (x <- blockCount / 2 until blockCount; y <- 0 until blockCount) {
  //   blocksVar.update { blocks =>
  //     blocks(x)(y) = 1
  //     blocks
  //   }
  // }
  // Fill blocks randomly
  for (x <- 0 until blockCount; y <- 0 until blockCount) {
    blocksVar.update { blocks =>
      blocks(x)(y) = if scala.util.Random.nextBoolean() then 1 else 0
      blocks
    }
  }
  val c =
    canvasTag(idAttr("pong-canvas"), widthAttr := 1024, heightAttr := 1024)
  tickStream.foreach { _ =>
    val ctx = c.ref
      .getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]
    ctx
      .clearRect(0, 0, c.ref.width, c.ref.height)
    ctx.fillStyle = "rgb(64, 64, 64)"
    ctx.fillRect(0, 0, c.ref.width, c.ref.height)

    // draw blocks
    for (x <- 0 until blockCount; y <- 0 until blockCount) {
      if blocksVar.now()(x)(y) == 0 then
        ctx.fillStyle = "rgb(64, 64, 64)"
        ctx.fillRect(x * blockSize, y * blockSize, blockSize, blockSize)
      else if blocksVar.now()(x)(y) == 1 then
        ctx.fillStyle = "rgb(194, 194, 194)"
        ctx.fillRect(x * blockSize, y * blockSize, blockSize, blockSize)
    }

    ctx.fillStyle = "rgb(200, 0, 0)"
    val (x, y, ax, ay) = ballVar.now()
    ctx.beginPath()
    ctx.fillRect(x, y, ballDiameter, ballDiameter)
    ctx.fill()
    ctx.closePath()

    ctx.fillStyle = "rgb(0, 200, 0)"
    val (x2, y2, ax2, ay2) = ballVar2.now()
    ctx.beginPath()
    ctx.fillRect(x2, y2, ballDiameter, ballDiameter)
    ctx.fill()
    ctx.closePath()

    // collide boundary
    if x < 0 || x > c.ref.width - ballDiameter then
      ballVar.update { case (x, y, ax, ay) => (x - ax, y, -ax, ay) }
    if y < 0 || y > c.ref.height - ballDiameter then
      ballVar.update { case (x, y, ax, ay) => (x, y - ay, ax, -ay) }

    if x2 < 0 || x2 > c.ref.width - ballDiameter then
      ballVar2.update { case (x, y, ax, ay) => (x - ax, y, -ax, ay) }
    if y2 < 0 || y2 > c.ref.height - ballDiameter then
      ballVar2.update { case (x, y, ax, ay) => (x, y - ay, ax, -ay) }

    // ball collides block. if collide from left or right, reverse x direction. if collide from top or bottom, reverse y direction.
    val bx = x / blockSize
    val by = y / blockSize
    if bx >= 0 && bx < blockCount && by >= 0 && by < blockCount && blocksVar
        .now()(bx)(by) == 1
    then
      blocksVar.update { blocks =>
        blocks(bx)(by) = 0
        blocks
      }
      if bx * blockSize <= x && x <= bx * blockSize + blockSize then
        ballVar.update { case (x, y, ax, ay) => (x - ax, y, -ax, ay) }
      else ballVar.update { case (x, y, ax, ay) => (x, y - ay, ax, -ay) }

    val bx2 = x2 / blockSize
    val by2 = y2 / blockSize
    if bx2 >= 0 && bx2 < blockCount && by2 >= 0 && by2 < blockCount && blocksVar
        .now()(bx2)(by2) == 0
    then
      blocksVar.update { blocks =>
        blocks(bx2)(by2) = 1
        blocks
      }
      if bx2 * blockSize <= x2 && x2 <= bx2 * blockSize + blockSize then
        ballVar2.update { case (x, y, ax, ay) => (x - ax, y, -ax, ay) }
      else ballVar2.update { case (x, y, ax, ay) => (x, y - ay, ax, -ay) }

    ballVar.update { case (x, y, ax, ay) => (x + ax, y + ay, ax, ay) }
    ballVar2.update { case (x, y, ax, ay) => (x + ax, y + ay, ax, ay) }

  }(unsafeWindowOwner)
  val appElement: Div = div(
    h1("Hello"),
    c
  )
  val root: RootNode = render(appContainer, appElement)
