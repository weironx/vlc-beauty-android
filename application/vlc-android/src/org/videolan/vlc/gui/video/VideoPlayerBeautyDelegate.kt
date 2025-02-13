package org.videolan.vlc.gui.video

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import com.hapi.avcapture.FrameCall
import com.hapi.avcapture.HapiTrackFactory
import com.hapi.avparam.ImgFmt
import com.hapi.avparam.VideoFrame
import com.hapi.avrender.CaptureVideoSizeAdapter
import com.hapi.avrender.HapiCapturePreView
import com.hapi.avrender.OpenGLRender
import com.hapi.pixelfree.PFDetectFormat
import com.hapi.pixelfree.PFIamgeInput
import com.hapi.pixelfree.PFRotationMode
import com.hapi.pixelfree.PixelFree
import com.hapi.pixelfreeuikit.PixeBeautyDialog
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import org.videolan.vlc.PlaybackService
import org.videolan.vlc.R

class VideoPlayerBeautyDelegate(
    private val player: VideoPlayerActivity,
    private val videoLayout: VLCVideoLayout,
    private val playbackService: PlaybackService
) {

    private val mPixelFree by lazy {
        PixelFree()
    }
    val mOpenGLRender by lazy { OpenGLRender() }


    fun beauty() {

        videoLayout?.findViewById<FrameLayout>(R.id.player_surface_frame)?.let {
            val surfaceView = it.findViewById<SurfaceView>(R.id.surface_video)

            surfaceView?.let { surface ->
                var w = 0
                var h = 0
                playbackService?.currentVideoTrack?.let {
                    w = it.getWidth()
                    h = it.getHeight()
                }
                if (w == 0) w = surface.width
                if (h == 0) h = surface.height

                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)


                var rgbaData = convertBitmapToRGBA(bitmap);
                var rowBytes = bitmap.rowBytes;

                mOpenGLRender.onFrame(1,w,h,ImgFmt.IMAGE_FORMAT_RGBA,rowBytes,rgbaData)

                val bitmap2 = BitmapFactory.decodeByteArray(rgbaData, 0, rgbaData.size)
                bitmap2
            }
        }
    }




    fun convertBitmapToRGBA(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val rgbaDataa = IntArray(width * height)

        bitmap.getPixels(rgbaDataa, 0, width, 0, 0, width, height)
        return intArrayToByteArray(rgbaDataa)
    }


    fun intArrayToByteArray(rgbaData: IntArray): ByteArray {
        // 创建一个 ByteArray，大小为 RGBA 数据的四倍（每个像素四个字节）
        val byteArray = ByteArray(rgbaData.size * 4)

        for (i in rgbaData.indices) {
            // 获取 RGBA 颜色
            val color = rgbaData[i]

            // 解析 R、G、B、A 通道
            val a = (color shr 24 and 0xFF).toByte() // Alpha
            val r = (color shr 16 and 0xFF).toByte() // Red
            val g = (color shr 8 and 0xFF).toByte()  // Green
            val b = (color and 0xFF).toByte()        // Blue

            // 将 RGBA 值存入 ByteArray
            byteArray[i * 4] = r
            byteArray[i * 4 + 1] = g
            byteArray[i * 4 + 2] = b
            byteArray[i * 4 + 3] = a
        }

        return byteArray
    }

    fun byteArrayToIntArray(byteArray: ByteArray): IntArray {
        val intArray = IntArray(byteArray.size / 4) // 每 4 个字节对应一个 Int

        for (i in intArray.indices) {
            // 组合 R、G、B、A 值为一个 Int
            val r = byteArray[i * 4].toInt() and 0xFF
            val g = byteArray[i * 4 + 1].toInt() and 0xFF
            val b = byteArray[i * 4 + 2].toInt() and 0xFF
            val a = byteArray[i * 4 + 3].toInt() and 0xFF

            intArray[i] = (a shl 24) or (r shl 16) or (g shl 8) or b // 组合成 ARGB 格式
        }

        return intArray
    }

}