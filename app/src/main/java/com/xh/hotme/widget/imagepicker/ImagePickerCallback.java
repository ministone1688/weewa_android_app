package com.xh.hotme.widget.imagepicker;

public interface ImagePickerCallback {
	void onImagePicked(String file);

	void onImagePickingCancelled();
}
