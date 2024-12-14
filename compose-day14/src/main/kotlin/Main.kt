import TestOrPuzzle.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.image.BufferedImage
import java.lang.Integer.max
import java.util.*

enum class TestOrPuzzle {
    Test, Puzzle, PuzzleSelected
}

data class InputData(val robots: List<Robot>, val width: Int, val height: Int)

val sizeOne = Size(1f, 1f)

@OptIn(ExperimentalUnsignedTypes::class)
@Composable
@Preview
fun App() {
    val robotsMap = remember {
        EnumMap<TestOrPuzzle, InputData>(TestOrPuzzle::class.java).apply {
            this[Test] = InputData(processInput(readInput("Day14_test")), 11, 7)
            val puzzleWidth = 101
            val puzzleHeight = 107
            val puzzleInputData = InputData(processInput(readInput("Day14")), puzzleWidth, puzzleHeight)
            this[Puzzle] = puzzleInputData
            this[PuzzleSelected] = puzzleInputData.copy(robots = puzzleInputData.robots.map {
                with(it) {
                    Robot(
                        position(2, puzzleWidth, puzzleHeight),
                        XAndY(v.x * 101 posRemainder puzzleWidth, v.y * 101 posRemainder puzzleHeight)
                    )
                }
            })
        }
    }
    var testOrPuzzle by remember { mutableStateOf(Test) }

    MaterialTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                testOrPuzzle = when (testOrPuzzle) {
                    Test -> Puzzle
                    Puzzle -> PuzzleSelected
                    PuzzleSelected -> Test
                }
            }) {
                Text(
                    when (testOrPuzzle) {
                        Test -> "Test input"
                        Puzzle -> "Puzzle input"
                        PuzzleSelected -> "Puzzle input starting from 2 with interval 101"
                    }
                )
            }

            val inputData = robotsMap.getValue(testOrPuzzle)

            val minSize = 100
            val dpZoom = (minSize / with(inputData) { max(width, height) }).coerceAtLeast(1)
                .also { println("DP zoom: $it") }
            val pxZoom = (dpZoom * LocalDensity.current.density).also { println("px zoom: $it") }
            val composeImageSize = DpSize((inputData.width * dpZoom).dp, (inputData.height * dpZoom).dp).also {
                println("canvas size: $it")
            }
            //val pointSize = Size(pxZoom, pxZoom)

            Box {
                val seconds = 0..10000

                /*
                //remember(inputData)
                //println("Computing visuals beforehand")
                val visuals =
                    seconds.map { i ->
                        with(inputData) {
                            robots.positions(i, width, height).toCountVisual(width, height)
                        } // cache the visuals first so it's faster when scrolling
                    }
                */

                /*
                println("Drawing bitmaps beforehand")
                val imageBitmaps = with(inputData) {
                    seconds.map { i ->
                        val visual = robots.positions(i, width, height).toCountVisual(width, height)

                        // `ImageBitmap` is immutable?
                        ImageBitmap(width, height, ImageBitmapConfig.Alpha8).also {
                            Canvas(it).run {
                                // copied and adapted from below
                                for ((y, line) in visual.withIndex())
                                    for ((x, count) in line.withIndex()) {
                                        val colorByte = count.coerceAtMost(3U) * 85U
                                        val colorByteInInt = colorByte.toInt()
                                        val color = Color(colorByteInInt, colorByteInInt, colorByteInInt)
                                        drawRect(x.toFloat(), y.toFloat(), x.toFloat(), y.toFloat(), Paint().apply {
                                            this.color = color
                                        })
                                    }
                            }
                        }
                    }
                }
                */

                /*
                println("Drawing canvases beforehand")
                val canvases = with(inputData) {
                    seconds.map { i ->
                        val visual = robots.positions(i, width, height).toCountVisual(width, height)

                        // `ImageBitmap` is immutable
                        Canvas(ImageBitmap(width, height, ImageBitmapConfig.Alpha8)).apply {
                            // copied and adapted from below
                            for ((y, line) in visual.withIndex())
                                for ((x, count) in line.withIndex()) {
                                    val colorByte = count.coerceAtMost(3U) * 85U
                                    val colorByteInInt = colorByte.toInt()
                                    val color = Color(colorByteInInt, colorByteInInt, colorByteInInt)
                                    drawRect(x.toFloat(), y.toFloat(), x.toFloat(), y.toFloat(), Paint().apply {
                                        this.color = color
                                    })
                                }
                        }
                    }
                }
                */

                println("Drawing painters with `BufferedImage`s beforehand")
                val painters = with(inputData) {
                    seconds.map { i ->
                        val visual = robots.positions(i, width, height).toCountVisual(width, height)

                        BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY).apply {
                            // copied and adapted from below
                            for ((y, line) in visual.withIndex())
                                for ((x, count) in line.withIndex()) {
                                    val colorByte = count.coerceAtMost(3U) * 85U
                                    val colorByteInInt = colorByte.toInt()
                                    val color = java.awt.Color(colorByteInInt, colorByteInInt, colorByteInInt).rgb
                                    setRGB(x, y, color)
                                }
                        }.toPainter().apply {

                        }
                    }
                }

                val lazyGridState = rememberLazyGridState()
                LazyVerticalGrid(
                    GridCells.Adaptive(minSize.dp),
                    state = lazyGridState,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(seconds.toList()) { i ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(i.toString())

                            /*
                            val visual = visuals[i]
                            Canvas(Modifier.size(composeCanvasSize)) {
                                scale(pxZoom, Offset.Zero) {
                                    for ((y, line) in visual.withIndex())
                                        for ((x, count) in line.withIndex()) {
                                            val colorByte = count.coerceAtMost(3U) * 85U
                                            val colorByteInInt = colorByte.toInt()
                                            val color = Color(colorByteInInt, colorByteInInt, colorByteInInt)
                                            drawRect(color, Offset(x.toFloat(), y.toFloat()), sizeOne)
                                        }
                                }
                            }
                            */

                            //Image(imageBitmaps[i], i.toString()/*, Modifier.size(composeCanvasSize)*/)

                            //Image(canvases[i])
                            //Canvas(canvases[i])
                            /*
                            Canvas(Modifier.size(composeCanvasSize)) {
                                scale(pxZoom, Offset.Zero) {

                                }
                            }
                            */

                            Image(painters[i], i.toString(), Modifier.size(composeImageSize)) // TODO don't blur
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = lazyGridState)
                )
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
