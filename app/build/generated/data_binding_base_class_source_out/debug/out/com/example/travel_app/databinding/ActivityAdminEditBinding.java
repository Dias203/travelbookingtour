// Generated by view binder compiler. Do not edit!
package com.example.travel_app.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.travel_app.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAdminEditBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView backBtn;

  @NonNull
  public final CardView cardView;

  @NonNull
  public final EditText editAddressTour;

  @NonNull
  public final EditText editBedNum;

  @NonNull
  public final EditText editCategoryId;

  @NonNull
  public final EditText editDateTour;

  @NonNull
  public final EditText editDescriptionTour;

  @NonNull
  public final EditText editDistanceTour;

  @NonNull
  public final EditText editDurationTour;

  @NonNull
  public final EditText editPriceTour;

  @NonNull
  public final EditText editScoreTour;

  @NonNull
  public final EditText editTimeTour;

  @NonNull
  public final EditText editTitleTour;

  @NonNull
  public final EditText editTourGuideName;

  @NonNull
  public final EditText editTourGuidePhone;

  @NonNull
  public final ConstraintLayout main;

  @NonNull
  public final ImageView picTour;

  @NonNull
  public final CheckBox popularCheckBox;

  @NonNull
  public final CheckBox recommendCheckBox;

  @NonNull
  public final Button saveButton;

  @NonNull
  public final TextView textView5;

  @NonNull
  public final TextView tv3;

  private ActivityAdminEditBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView backBtn,
      @NonNull CardView cardView, @NonNull EditText editAddressTour, @NonNull EditText editBedNum,
      @NonNull EditText editCategoryId, @NonNull EditText editDateTour,
      @NonNull EditText editDescriptionTour, @NonNull EditText editDistanceTour,
      @NonNull EditText editDurationTour, @NonNull EditText editPriceTour,
      @NonNull EditText editScoreTour, @NonNull EditText editTimeTour,
      @NonNull EditText editTitleTour, @NonNull EditText editTourGuideName,
      @NonNull EditText editTourGuidePhone, @NonNull ConstraintLayout main,
      @NonNull ImageView picTour, @NonNull CheckBox popularCheckBox,
      @NonNull CheckBox recommendCheckBox, @NonNull Button saveButton, @NonNull TextView textView5,
      @NonNull TextView tv3) {
    this.rootView = rootView;
    this.backBtn = backBtn;
    this.cardView = cardView;
    this.editAddressTour = editAddressTour;
    this.editBedNum = editBedNum;
    this.editCategoryId = editCategoryId;
    this.editDateTour = editDateTour;
    this.editDescriptionTour = editDescriptionTour;
    this.editDistanceTour = editDistanceTour;
    this.editDurationTour = editDurationTour;
    this.editPriceTour = editPriceTour;
    this.editScoreTour = editScoreTour;
    this.editTimeTour = editTimeTour;
    this.editTitleTour = editTitleTour;
    this.editTourGuideName = editTourGuideName;
    this.editTourGuidePhone = editTourGuidePhone;
    this.main = main;
    this.picTour = picTour;
    this.popularCheckBox = popularCheckBox;
    this.recommendCheckBox = recommendCheckBox;
    this.saveButton = saveButton;
    this.textView5 = textView5;
    this.tv3 = tv3;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAdminEditBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAdminEditBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_admin_edit, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAdminEditBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.backBtn;
      ImageView backBtn = ViewBindings.findChildViewById(rootView, id);
      if (backBtn == null) {
        break missingId;
      }

      id = R.id.cardView;
      CardView cardView = ViewBindings.findChildViewById(rootView, id);
      if (cardView == null) {
        break missingId;
      }

      id = R.id.editAddressTour;
      EditText editAddressTour = ViewBindings.findChildViewById(rootView, id);
      if (editAddressTour == null) {
        break missingId;
      }

      id = R.id.editBedNum;
      EditText editBedNum = ViewBindings.findChildViewById(rootView, id);
      if (editBedNum == null) {
        break missingId;
      }

      id = R.id.editCategoryId;
      EditText editCategoryId = ViewBindings.findChildViewById(rootView, id);
      if (editCategoryId == null) {
        break missingId;
      }

      id = R.id.editDateTour;
      EditText editDateTour = ViewBindings.findChildViewById(rootView, id);
      if (editDateTour == null) {
        break missingId;
      }

      id = R.id.editDescriptionTour;
      EditText editDescriptionTour = ViewBindings.findChildViewById(rootView, id);
      if (editDescriptionTour == null) {
        break missingId;
      }

      id = R.id.editDistanceTour;
      EditText editDistanceTour = ViewBindings.findChildViewById(rootView, id);
      if (editDistanceTour == null) {
        break missingId;
      }

      id = R.id.editDurationTour;
      EditText editDurationTour = ViewBindings.findChildViewById(rootView, id);
      if (editDurationTour == null) {
        break missingId;
      }

      id = R.id.editPriceTour;
      EditText editPriceTour = ViewBindings.findChildViewById(rootView, id);
      if (editPriceTour == null) {
        break missingId;
      }

      id = R.id.editScoreTour;
      EditText editScoreTour = ViewBindings.findChildViewById(rootView, id);
      if (editScoreTour == null) {
        break missingId;
      }

      id = R.id.editTimeTour;
      EditText editTimeTour = ViewBindings.findChildViewById(rootView, id);
      if (editTimeTour == null) {
        break missingId;
      }

      id = R.id.editTitleTour;
      EditText editTitleTour = ViewBindings.findChildViewById(rootView, id);
      if (editTitleTour == null) {
        break missingId;
      }

      id = R.id.editTourGuideName;
      EditText editTourGuideName = ViewBindings.findChildViewById(rootView, id);
      if (editTourGuideName == null) {
        break missingId;
      }

      id = R.id.editTourGuidePhone;
      EditText editTourGuidePhone = ViewBindings.findChildViewById(rootView, id);
      if (editTourGuidePhone == null) {
        break missingId;
      }

      ConstraintLayout main = (ConstraintLayout) rootView;

      id = R.id.picTour;
      ImageView picTour = ViewBindings.findChildViewById(rootView, id);
      if (picTour == null) {
        break missingId;
      }

      id = R.id.popularCheckBox;
      CheckBox popularCheckBox = ViewBindings.findChildViewById(rootView, id);
      if (popularCheckBox == null) {
        break missingId;
      }

      id = R.id.recommendCheckBox;
      CheckBox recommendCheckBox = ViewBindings.findChildViewById(rootView, id);
      if (recommendCheckBox == null) {
        break missingId;
      }

      id = R.id.saveButton;
      Button saveButton = ViewBindings.findChildViewById(rootView, id);
      if (saveButton == null) {
        break missingId;
      }

      id = R.id.textView5;
      TextView textView5 = ViewBindings.findChildViewById(rootView, id);
      if (textView5 == null) {
        break missingId;
      }

      id = R.id.tv3;
      TextView tv3 = ViewBindings.findChildViewById(rootView, id);
      if (tv3 == null) {
        break missingId;
      }

      return new ActivityAdminEditBinding((ConstraintLayout) rootView, backBtn, cardView,
          editAddressTour, editBedNum, editCategoryId, editDateTour, editDescriptionTour,
          editDistanceTour, editDurationTour, editPriceTour, editScoreTour, editTimeTour,
          editTitleTour, editTourGuideName, editTourGuidePhone, main, picTour, popularCheckBox,
          recommendCheckBox, saveButton, textView5, tv3);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
