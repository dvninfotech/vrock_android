package Jni;



import VideoHandle.OnEditorListener;


public class FFmpegCmd {

	static {
		System.loadLibrary("avutil");
		System.loadLibrary("avcodec");
		System.loadLibrary("swresample");
		System.loadLibrary("avformat");
		System.loadLibrary("swscale");
		System.loadLibrary("avfilter");
		System.loadLibrary("avdevice");
		System.loadLibrary("ffmpeg-exec");
	}

	private static OnEditorListener listener;
	private static long duration;


	public static native int exec(int argc, String[] argv);


	public static native void exit();


	public static void onExecuted(int ret) {
		if (listener != null) {
			if (ret == 0) {
				listener.onProgress(1);
				listener.onSuccess();
				listener = null;
			} else {
				listener.onFailure();
				listener = null;
			}
		}
	}


	public static void onProgress(float progress) {
		if (listener != null) {
			if (duration != 0) {
				listener.onProgress(progress / (duration / 1000000) * 0.95f);
			}
		}
	}



	public static void exec(String[] cmds, long duration, OnEditorListener listener) {
		FFmpegCmd.listener = listener;
		FFmpegCmd.duration = duration;
		exec(cmds.length, cmds);
	}
}
