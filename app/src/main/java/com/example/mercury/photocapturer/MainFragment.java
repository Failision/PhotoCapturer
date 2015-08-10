package com.example.mercury.photocapturer;

import android.app.Fragment;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;


/**
 * Created by mercury on 7/30/2015.
 */
public class MainFragment extends Fragment implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ImageView captureButton;
    private ImageView changeCameraImage;

    private Camera camera;
    private OrientationEventListener OrientationListener;

    private int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int displayRotation;
    private int rotateFrom = 0;
    private int rotateFromAnti = 0;
    private boolean takePictureIsRunning = false;

    @Override
    public void onResume() {
        super.onResume();
        try {
            camera = Camera.open(CAMERA_ID);
            setCameraPreviewDisplayOrientation(CAMERA_ID);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch (Exception e) { Log.e("MyError", "Not changed"); }

        displayRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        OrientationListener.enable();
        if(CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK) {
            setPreviewSize();
        }
        Log.d("TAAG", "onResume");
    }

    @Override
    public void onPause() {
        OrientationListener.disable();
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        Log.d("TAAG", "onPause");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        captureButton = (ImageView) rootView.findViewById(R.id.id_button_photo);
        surfaceView = (SurfaceView) rootView.findViewById(R.id.surface_view);
        changeCameraImage = (ImageView) rootView.findViewById(R.id.change_camera_view);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        OrientationListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                rotateViewWithOrientation(orientation);
            }
        };
        return rootView;
    }

    public void callBufferFragment() {
        Fragment bufFragment = new BufferFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.photo_captured, bufFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        Log.d("TAAG", "callBufferFragment");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TAAG", "onViewCreated");

        captureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!takePictureIsRunning) {
                    callBufferFragment();
                    camera.takePicture(null, null, takingPicture);
                    takePictureIsRunning = true;
                }
            }
        });

        changeCameraImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
                if (CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK)
                    CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_FRONT;
                else
                    CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;

                try {
                    camera = Camera.open(CAMERA_ID);
                    setCameraPreviewDisplayOrientation(CAMERA_ID);
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    Log.d("MyError", "Not changed");
                }
            }
        });
    }

    Camera.PictureCallback takingPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Fragment bufFragment = getFragmentManager().findFragmentById(R.id.photo_captured);

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            if(CAMERA_ID == Camera.CameraInfo.CAMERA_FACING_BACK){
                matrix.setRotate(90, width / 2, height / 2);
            }else matrix.setRotate(-90, width / 2, height / 2);

            Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);

            ((ImageView) bufFragment.getView().findViewById(R.id.id_picture)).setImageBitmap(rotateBitmap);
            ((ProgressBar) bufFragment.getView().findViewById(R.id.progress_bar)).setVisibility(View.INVISIBLE);
            takePictureIsRunning = false;
            camera.startPreview();
        }
    };

    void setPreviewSize() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        if (widthIsMax) {
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        matrix.setRectToRect(rectPreview, rectDisplay, Matrix.ScaleToFit.START);
        //matrix.setRectToRect(rectDisplay, rectPreview, Matrix.ScaleToFit.START);
        matrix.invert(matrix);
        matrix.mapRect(rectPreview);

        surfaceView.getLayoutParams().height = (int) (rectPreview.bottom);
        surfaceView.getLayoutParams().width = (int) (rectPreview.right);
    }

    public void setCameraPreviewDisplayOrientation(int cameraId) {
        int degrees = 0;
        int result = 0;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = ((360 - degrees) - info.orientation);
            result += 360;
        }
        camera.setDisplayOrientation(result % 360);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.e("MyError", "surfaceCreated error");
        }
        Log.d("TAAG", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        camera.stopPreview();
        setCameraPreviewDisplayOrientation(CAMERA_ID);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.e("MyError", "surfaceChanged error");
        }
        camera.startPreview();
        Log.d("TAAG", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("TAAG", "surfaceDestroy");
    }

    public RotateAnimation createAnimation(float from, float to, int width, int height) {
        RotateAnimation r = new RotateAnimation(from, to, width, height);
        r.setDuration(500);
        r.setFillEnabled(true);
        r.setFillAfter(true);
        return r;
    }

    public void setRotationAntiClockwise(int to, int from) {
        RotateAnimation r;
        if (to == from) {return; }
        r = createAnimation(from, to, captureButton.getWidth() / 2, captureButton.getHeight() / 2);
        captureButton.startAnimation(r);
        r = createAnimation(from, to, changeCameraImage.getWidth() / 2, changeCameraImage.getHeight() / 2);
        changeCameraImage.startAnimation(r);
        rotateFrom = (int) (-to);
        Log.d("current", String.valueOf(rotateFrom));
    }

    public void setRotationClockwise(int to, int from) {
        if (to == from) { return; }
        RotateAnimation r;
        r = createAnimation(from, to, captureButton.getWidth() / 2, captureButton.getHeight() / 2);
        captureButton.startAnimation(r);
        r = createAnimation(from, to, changeCameraImage.getWidth() / 2, changeCameraImage.getHeight() / 2);
        changeCameraImage.startAnimation(r);
        rotateFromAnti = (int) (to);
        Log.d("current", "ANTI " + String.valueOf(rotateFromAnti));
    }

    public void rotateViewWithOrientation(int degree) {
        if (degree == -1) return;
        if (degree > 50 & degree < 140) { setRotationAntiClockwise(-90, -rotateFrom); }
        if (degree > 140 & degree < 230) { setRotationAntiClockwise(-180, -rotateFrom); }
        if (degree < 50 & degree >= 0) { setRotationAntiClockwise(0, -rotateFrom); rotateFromAnti = 0; }
        if (degree > 230 & degree < 320) {
            if (rotateFrom == 0) { setRotationClockwise(90, rotateFromAnti); rotateFrom = 270; }
            else { rotateFromAnti = 90; setRotationAntiClockwise(-270, -rotateFrom); }
        }
        if (degree > 320) {
            if (rotateFrom == 180) { setRotationAntiClockwise(0, -rotateFrom); }
            else { setRotationClockwise(0, rotateFromAnti); rotateFrom = 0; }
        }
    }
}