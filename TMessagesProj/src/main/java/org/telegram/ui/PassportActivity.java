package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import com.lunamint.lunagram.BuildConfig;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MrzRecognizer;
import org.telegram.messenger.NotificationCenter;
import com.lunamint.lunagram.R;
import org.telegram.messenger.SecureDocument;
import org.telegram.messenger.SecureDocumentKey;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.SlideView;
import org.telegram.ui.Components.URLSpanNoUnderline;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;

public class PassportActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    public final static int TYPE_REQUEST = 0;
    public final static int TYPE_IDENTITY = 1;
    public final static int TYPE_ADDRESS = 2;
    public final static int TYPE_PHONE = 3;
    public final static int TYPE_EMAIL = 4;
    public final static int TYPE_PASSWORD = 5;
    public final static int TYPE_EMAIL_VERIFICATION = 6;
    public final static int TYPE_PHONE_VERIFICATION = 7;
    public final static int TYPE_MANAGE = 8;

    private final static int FIELD_NAME = 0;
    private final static int FIELD_SURNAME = 1;
    private final static int FIELD_BIRTHDAY = 2;
    private final static int FIELD_GENDER = 3;
    private final static int FIELD_CITIZENSHIP = 4;
    private final static int FIELD_RESIDENCE = 5;
    private final static int FIELD_CARDNUMBER = 6;
    private final static int FIELD_EXPIRE = 7;
    private final static int FIELD_IDENTITY_COUNT = 8;
    private final static int FIELD_IDENTITY_NODOC_COUNT = 6;

    private final static int FIELD_STREET1 = 0;
    private final static int FIELD_STREET2 = 1;
    private final static int FIELD_POSTCODE = 2;
    private final static int FIELD_CITY = 3;
    private final static int FIELD_STATE = 4;
    private final static int FIELD_COUNTRY = 5;
    private final static int FIELD_ADDRESS_COUNT = 6;

    private final static int FIELD_PHONECOUNTRY = 0;
    private final static int FIELD_PHONECODE = 1;
    private final static int FIELD_PHONE = 2;

    private final static int FIELD_EMAIL = 0;

    private final static int FIELD_PASSWORD = 0;

    private final static int UPLOADING_TYPE_DOCUMENTS = 0;
    private final static int UPLOADING_TYPE_SELFIE = 1;
    private final static int UPLOADING_TYPE_FRONT = 2;
    private final static int UPLOADING_TYPE_REVERSE = 3;

    private String initialValues;
    private int currentActivityType;
    private int currentBotId;
    private String currentPayload;
    private boolean useCurrentValue;
    private String currentScope;
    private String currentCallbackUrl;
    private String currentPublicKey;
    private String currentCitizeship = "";
    private String currentResidence = "";
    private String currentGender;
    private int[] currentExpireDate = new int[3];
    private TLRPC.TL_account_authorizationForm currentForm;

    private TLRPC.SecureValueType currentType;
    private TLRPC.SecureValueType currentDocumentsType;
    private ArrayList<TLRPC.SecureValueType> availableDocumentTypes;
    private TLRPC.TL_secureValue currentTypeValue;
    private TLRPC.TL_secureValue currentDocumentsTypeValue;
    private TLRPC.account_Password currentPassword;
    private TLRPC.TL_auth_sentCode currentPhoneVerification;

    private ActionBarMenuItem doneItem;
    private AnimatorSet doneItemAnimation;
    private ContextProgressView progressView;

    private TextView acceptTextView;
    private ContextProgressView progressViewButton;
    private FrameLayout bottomLayout;

    private TextSettingsCell uploadDocumentCell;
    private View extraBackgroundView;
    private TextDetailSettingsCell uploadFrontCell;
    private TextDetailSettingsCell uploadReverseCell;
    private TextDetailSettingsCell uploadSelfieCell;
    private EditTextBoldCursor[] inputFields;
    private ViewGroup[] inputFieldContainers;
    private ScrollView scrollView;
    private LinearLayout linearLayout2;
    private LinearLayout documentsLayout;
    private LinearLayout frontLayout;
    private LinearLayout reverseLayout;
    private LinearLayout selfieLayout;
    private LinearLayout currentPhotoViewerLayout;
    private HeaderCell headerCell;
    private ArrayList<View> dividers = new ArrayList<>();
    private ShadowSectionCell sectionCell;
    private TextInfoPrivacyCell bottomCell;
    private TextSettingsCell scanDocumentCell;

    private TextView plusTextView;

    private TextSettingsCell addDocumentCell;
    private TextSettingsCell deletePassportCell;
    private ShadowSectionCell addDocumentSectionCell;
    private LinearLayout emptyLayout;
    private ImageView emptyImageView;
    private TextView emptyTextView1;
    private TextView emptyTextView2;
    private TextView emptyTextView3;

    private EmptyTextProgressView emptyView;
    private TextInfoPrivacyCell passwordRequestTextView;
    private TextInfoPrivacyCell passwordInfoRequestTextView;
    private ImageView noPasswordImageView;
    private TextView noPasswordTextView;
    private TextView noPasswordSetTextView;
    private FrameLayout passwordAvatarContainer;
    private TextView passwordForgotButton;
    private int usingSavedPassword;
    private byte[] savedPasswordHash;
    private byte[] savedSaltedPassword;

    private String currentPicturePath;
    private ChatAttachAlert chatAttachAlert;
    private int uploadingFileType;

    private int emailCodeLength;

    private ArrayList<String> countriesArray = new ArrayList<>();
    private HashMap<String, String> countriesMap = new HashMap<>();
    private HashMap<String, String> codesMap = new HashMap<>();
    private HashMap<String, String> phoneFormatMap = new HashMap<>();
    private HashMap<String, String> languageMap;

    private boolean ignoreOnTextChange;
    private boolean ignoreOnPhoneChange;

    private static final int info_item = 1;
    private static final int done_button = 2;

    private final static int attach_photo = 0;
    private final static int attach_gallery = 1;
    private final static int attach_document = 4;

    private long secureSecretId;
    private byte[] secureSecret;
    private String currentEmail;
    private byte[] saltedPassword;

    private boolean ignoreOnFailure;
    private boolean callbackCalled;
    private PassportActivity presentAfterAnimation;

    private ArrayList<SecureDocument> documents = new ArrayList<>();
    private SecureDocument selfieDocument;
    private SecureDocument frontDocument;
    private SecureDocument reverseDocument;
    private HashMap<SecureDocument, SecureDocumentCell> documentsCells = new HashMap<>();
    private HashMap<String, SecureDocument> uploadingDocuments = new HashMap<>();
    private HashMap<TLRPC.SecureValueType, HashMap<String, String>> typesValues = new HashMap<>();
    private HashMap<TLRPC.SecureValueType, TextDetailSecureCell> typesViews = new HashMap<>();
    private HashMap<String, String> currentValues;
    private HashMap<String, HashMap<String, String>> errorsMap = new HashMap<>();
    private HashMap<String, String> fieldsErrors;
    private HashMap<String, String> documentsErrors;
    private HashMap<String, String> errorsValues = new HashMap<>();
    private CharSequence noAllDocumentsErrorText;

    private PassportActivityDelegate delegate;

    private boolean needActivityResult;

    private interface PassportActivityDelegate {
        void saveValue(TLRPC.SecureValueType type, String text, String json, TLRPC.SecureValueType documentsType, String documentsJson, ArrayList<SecureDocument> documents, SecureDocument selfie, SecureDocument front, SecureDocument reverse, Runnable finishRunnable, ErrorRunnable errorRunnable);
        void deleteValue(TLRPC.SecureValueType type, TLRPC.SecureValueType documentsType, boolean deleteType, Runnable finishRunnable, ErrorRunnable errorRunnable);
        SecureDocument saveFile(TLRPC.TL_secureFile secureFile);
    }

    private interface ErrorRunnable {
        void onError(String error, String text);
    }

    private PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

        @Override
        public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
            if (index < 0 || index >= currentPhotoViewerLayout.getChildCount()) {
                return null;
            }
            SecureDocumentCell cell = (SecureDocumentCell) currentPhotoViewerLayout.getChildAt(index);
            int coords[] = new int[2];
            cell.imageView.getLocationInWindow(coords);
            PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
            object.viewX = coords[0];
            object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
            object.parentView = currentPhotoViewerLayout;
            object.imageReceiver = cell.imageView.getImageReceiver();
            object.thumb = object.imageReceiver.getBitmapSafe();
            return object;
        }

        @Override
        public void deleteImageAtIndex(int index) {
            SecureDocument document;
            if (uploadingFileType == UPLOADING_TYPE_SELFIE) {
                document = selfieDocument;
            } else if (uploadingFileType == UPLOADING_TYPE_FRONT) {
                document = frontDocument;
            } else if (uploadingFileType == UPLOADING_TYPE_REVERSE) {
                document = reverseDocument;
            } else {
                document = documents.get(index);
            }
            SecureDocumentCell cell = documentsCells.remove(document);
            if (cell == null) {
                return;
            }
            String key = null;
            String hash = getDocumentHash(document);
            if (uploadingFileType == UPLOADING_TYPE_SELFIE) {
                selfieDocument = null;
                key = "selfie" + hash;
            } else if (uploadingFileType == UPLOADING_TYPE_FRONT) {
                frontDocument = null;
                key = "front" + hash;
            } else if (uploadingFileType == UPLOADING_TYPE_REVERSE) {
                reverseDocument = null;
                key = "reverse" + hash;
            } else if (uploadingFileType == UPLOADING_TYPE_DOCUMENTS) {
                key = "files" + hash;
            }

            if (key != null) {
                if (documentsErrors != null) {
                    documentsErrors.remove(key);
                }
                if (errorsValues != null) {
                    errorsValues.remove(key);
                }
            }

            updateUploadText(uploadingFileType);
            currentPhotoViewerLayout.removeView(cell);
        }

        @Override
        public String getDeleteMessageString() {
            if (uploadingFileType == UPLOADING_TYPE_SELFIE) {
                return LocaleController.formatString("PassportDeleteSelfieAlert", R.string.PassportDeleteSelfieAlert);
            } else {
                return LocaleController.formatString("PassportDeleteScanAlert", R.string.PassportDeleteScanAlert);
            }
        }
    };

    public class LinkSpan extends ClickableSpan {
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(true);
            ds.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        }

        @Override
        public void onClick(View widget) {
            Browser.openUrl(getParentActivity(), currentForm.privacy_policy_url);
        }
    }

    public class TextDetailSecureCell extends FrameLayout {

        private TextView textView;
        private TextView valueTextView;
        private ImageView checkImageView;
        private boolean needDivider;

        public TextDetailSecureCell(Context context) {
            super(context);

            int padding = currentActivityType == TYPE_MANAGE ? 17 : 47;

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? padding : 17), 10, (LocaleController.isRTL ? 17 : padding), 0));

            valueTextView = new TextView(context);
            valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            valueTextView.setLines(1);
            valueTextView.setMaxLines(1);
            valueTextView.setSingleLine(true);
            valueTextView.setEllipsize(TextUtils.TruncateAt.END);
            valueTextView.setPadding(0, 0, 0, 0);
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? padding : 17), 35, (LocaleController.isRTL ? 17 : padding), 0));

            checkImageView = new ImageView(context);
            checkImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_addedIcon), PorterDuff.Mode.MULTIPLY));
            checkImageView.setImageResource(R.drawable.sticker_added);
            addView(checkImageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 17, 25, 17, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
        }

        public void setTextAndValue(String text, CharSequence value, boolean divider) {
            textView.setText(text);
            valueTextView.setText(value);
            needDivider = divider;
            setWillNotDraw(!divider);
        }

        public void setChecked(boolean checked) {
            checkImageView.setVisibility(checked ? VISIBLE : INVISIBLE);
        }

        public void setValue(CharSequence value) {
            valueTextView.setText(value);
        }

        public void setNeedDivider(boolean value) {
            needDivider = value;
            setWillNotDraw(!needDivider);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (needDivider) {
                canvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
            }
        }
    }

    public class SecureDocumentCell extends FrameLayout implements DownloadController.FileDownloadProgressListener {

        private TextView textView;
        private TextView valueTextView;
        private BackupImageView imageView;
        private RadialProgress radialProgress;

        private int buttonState;
        private SecureDocument currentSecureDocument;

        private int TAG;

        public SecureDocumentCell(Context context) {
            super(context);

            TAG = DownloadController.getInstance(currentAccount).generateObserverTag();
            radialProgress = new RadialProgress(this);

            imageView = new BackupImageView(context);
            addView(imageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 17, 8, 17, 0));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 17 : 77), 10, (LocaleController.isRTL ? 77 : 17), 0));

            valueTextView = new TextView(context);
            valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            valueTextView.setLines(1);
            valueTextView.setMaxLines(1);
            valueTextView.setSingleLine(true);
            valueTextView.setPadding(0, 0, 0, 0);
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 17 : 77), 35, (LocaleController.isRTL ? 77 : 17), 0));

            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64) + 1, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            int x = imageView.getLeft() + (imageView.getMeasuredWidth() - AndroidUtilities.dp(24)) / 2;
            int y = imageView.getTop() + (imageView.getMeasuredHeight() - AndroidUtilities.dp(24)) / 2;
            radialProgress.setProgressRect(x, y, x + AndroidUtilities.dp(24), y + AndroidUtilities.dp(24));
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean result = super.drawChild(canvas, child, drawingTime);
            if (child == imageView) {
                radialProgress.draw(canvas);
            }
            return result;
        }

        public void setTextAndValueAndImage(String text, CharSequence value, SecureDocument document) {
            textView.setText(text);
            valueTextView.setText(value);
            imageView.setImage(document, "48_48");
            currentSecureDocument = document;

            updateButtonState(false);
        }

        public void setValue(CharSequence value) {
            valueTextView.setText(value);
        }

        public void updateButtonState(boolean animated) {
            String fileName = FileLoader.getAttachFileName(currentSecureDocument);
            File path = FileLoader.getPathToAttach(currentSecureDocument);
            boolean fileExists = path.exists();
            if (TextUtils.isEmpty(fileName)) {
                radialProgress.setBackground(null, false, false);
                return;
            }

            if (currentSecureDocument.path != null) {
                if (currentSecureDocument.inputFile != null) {
                    DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                    radialProgress.setBackground(null, false, animated);
                    buttonState = -1;
                } else {
                    DownloadController.getInstance(currentAccount).addLoadingFileObserver(currentSecureDocument.path, this);
                    buttonState = 1;
                    Float progress = ImageLoader.getInstance().getFileProgress(currentSecureDocument.path);
                    radialProgress.setBackground(Theme.chat_photoStatesDrawables[5][0], true, animated);
                    radialProgress.setProgress(progress != null ? progress : 0, false);
                    invalidate();
                }
            } else {
                if (fileExists) {
                    DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                    buttonState = -1;
                    radialProgress.setBackground(null, false, animated);
                    invalidate();
                } else {
                    DownloadController.getInstance(currentAccount).addLoadingFileObserver(fileName, this);
                    buttonState = 1;
                    Float progress = ImageLoader.getInstance().getFileProgress(fileName);
                    radialProgress.setBackground(Theme.chat_photoStatesDrawables[5][0], true, animated);
                    radialProgress.setProgress(progress != null ? progress : 0, animated);
                    invalidate();
                }
            }
        }

        @Override
        public void invalidate() {
            super.invalidate();
            textView.invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, Theme.dividerPaint);
        }

        @Override
        public void onFailedDownload(String fileName) {
            updateButtonState(false);
        }

        @Override
        public void onSuccessDownload(String fileName) {
            radialProgress.setProgress(1, true);
            updateButtonState(true);
        }

        @Override
        public void onProgressDownload(String fileName, float progress) {
            radialProgress.setProgress(progress, true);
            if (buttonState != 1) {
                updateButtonState(false);
            }
        }

        @Override
        public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
            radialProgress.setProgress(progress, true);
        }

        @Override
        public int getObserverTag() {
            return TAG;
        }
    }

    public PassportActivity(int type, int botId, String scope, String publicKey, String payload, String callbackUrl, TLRPC.TL_account_authorizationForm form, TLRPC.account_Password accountPassword) {
        this(type, form, accountPassword, null, null, null, null, null);
        currentBotId = botId;
        currentPayload = payload;
        currentScope = scope;
        currentPublicKey = publicKey;
        currentCallbackUrl = callbackUrl;
        if (type == TYPE_REQUEST) {
            if (!form.errors.isEmpty()) {
                try {
                    for (int a = 0, size = form.errors.size(); a < size; a++) {
                        TLRPC.SecureValueError secureValueError = form.errors.get(a);
                        String key;
                        String description;
                        String target;

                        String field = null;
                        byte[] file_hash = null;

                        if (secureValueError instanceof TLRPC.TL_secureValueErrorFrontSide) {
                            TLRPC.TL_secureValueErrorFrontSide secureValueErrorFrontSide = (TLRPC.TL_secureValueErrorFrontSide) secureValueError;
                            key = getNameForType(secureValueErrorFrontSide.type);
                            description = secureValueErrorFrontSide.text;
                            file_hash = secureValueErrorFrontSide.file_hash;
                            target = "front";
                        } else if (secureValueError instanceof TLRPC.TL_secureValueErrorReverseSide) {
                            TLRPC.TL_secureValueErrorReverseSide secureValueErrorReverseSide = (TLRPC.TL_secureValueErrorReverseSide) secureValueError;
                            key = getNameForType(secureValueErrorReverseSide.type);
                            description = secureValueErrorReverseSide.text;
                            file_hash = secureValueErrorReverseSide.file_hash;
                            target = "reverse";
                        } else if (secureValueError instanceof TLRPC.TL_secureValueErrorSelfie) {
                            TLRPC.TL_secureValueErrorSelfie secureValueErrorSelfie = (TLRPC.TL_secureValueErrorSelfie) secureValueError;
                            key = getNameForType(secureValueErrorSelfie.type);
                            description = secureValueErrorSelfie.text;
                            file_hash = secureValueErrorSelfie.file_hash;
                            target = "selfie";
                        } else if (secureValueError instanceof TLRPC.TL_secureValueErrorFile) {
                            TLRPC.TL_secureValueErrorFile secureValueErrorFile = (TLRPC.TL_secureValueErrorFile) secureValueError;
                            key = getNameForType(secureValueErrorFile.type);
                            description = secureValueErrorFile.text;
                            file_hash = secureValueErrorFile.file_hash;
                            target = "files";
                        } else if (secureValueError instanceof TLRPC.TL_secureValueErrorFiles) {
                            TLRPC.TL_secureValueErrorFiles secureValueErrorFiles = (TLRPC.TL_secureValueErrorFiles) secureValueError;
                            key = getNameForType(secureValueErrorFiles.type);
                            description = secureValueErrorFiles.text;
                            target = "files";
                        } else if (secureValueError instanceof TLRPC.TL_secureValueErrorData) {
                            TLRPC.TL_secureValueErrorData secureValueErrorData = (TLRPC.TL_secureValueErrorData) secureValueError;
                            boolean found = false;
                            for (int b = 0; b < form.values.size(); b++) {
                                TLRPC.TL_secureValue value = form.values.get(b);
                                if (value.data != null && Arrays.equals(value.data.data_hash, secureValueErrorData.data_hash)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                continue;
                            }
                            key = getNameForType(secureValueErrorData.type);
                            description = secureValueErrorData.text;
                            field = secureValueErrorData.field;
                            file_hash = secureValueErrorData.data_hash;
                            target = "data";
                        } else {
                            continue;
                        }
                        HashMap<String, String> vals = errorsMap.get(key);
                        if (vals == null) {
                            vals = new HashMap<>();
                            errorsMap.put(key, vals);
                        }
                        String hash;
                        if (file_hash != null) {
                            hash = Base64.encodeToString(file_hash, Base64.NO_WRAP);
                        } else {
                            hash = "";
                        }
                        if ("data".equals(target)) {
                            if (field != null) {
                                vals.put(field, description);
                            }
                        } else if ("files".equals(target)) {
                            if (file_hash != null) {
                                vals.put("files" + hash, description);
                            } else {
                                vals.put("files_all", description);
                            }
                        } else if ("selfie".equals(target)) {
                            vals.put("selfie" + hash, description);
                        } else if ("front".equals(target)) {
                            vals.put("front" + hash, description);
                        } else if ("reverse".equals(target)) {
                            vals.put("reverse" + hash, description);
                        }
                    }
                } catch (Exception ignore) {

                }
            }
        }
    }

    public PassportActivity(int type, TLRPC.TL_account_authorizationForm form, TLRPC.account_Password accountPassword, TLRPC.SecureValueType secureType, TLRPC.TL_secureValue secureValue, TLRPC.SecureValueType secureDocumentsType, TLRPC.TL_secureValue secureDocumentsValue, HashMap<String, String> values) {
        super();
        currentActivityType = type;
        currentForm = form;
        currentType = secureType;
        currentTypeValue = secureValue;
        currentDocumentsType = secureDocumentsType;
        currentDocumentsTypeValue = secureDocumentsValue;
        currentPassword = accountPassword;
        currentValues = values;
        if (currentActivityType == TYPE_PHONE) {
            permissionsItems = new ArrayList<>();
        } else if (currentActivityType == TYPE_PHONE_VERIFICATION) {
            views = new SlideView[3];
        }
        if (currentValues == null) {
            currentValues = new HashMap<>();
        }
        if (type == TYPE_PASSWORD) {
            if (UserConfig.getInstance(currentAccount).savedPasswordHash != null && UserConfig.getInstance(currentAccount).savedSaltedPassword != null) {
                usingSavedPassword = 1;
                savedPasswordHash = UserConfig.getInstance(currentAccount).savedPasswordHash;
                savedSaltedPassword = UserConfig.getInstance(currentAccount).savedSaltedPassword;
            }
            if (currentPassword == null) {
                loadPasswordInfo();
            } else {
                byte[] salt = new byte[currentPassword.new_salt.length + 8];
                Utilities.random.nextBytes(salt);
                System.arraycopy(currentPassword.new_salt, 0, salt, 0, currentPassword.new_salt.length);
                currentPassword.new_salt = salt;
            }
            if (usingSavedPassword == 1) {
                onPasswordDone(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chatAttachAlert != null) {
            chatAttachAlert.onResume();
        }
        if (currentActivityType == TYPE_PASSWORD && inputFieldContainers != null && inputFieldContainers[FIELD_PASSWORD] != null && inputFieldContainers[FIELD_PASSWORD].getVisibility() == View.VISIBLE) {
            inputFields[FIELD_PASSWORD].requestFocus();
            AndroidUtilities.showKeyboard(inputFields[FIELD_PASSWORD]);
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (inputFieldContainers != null && inputFieldContainers[FIELD_PASSWORD] != null && inputFieldContainers[FIELD_PASSWORD].getVisibility() == View.VISIBLE) {
                        inputFields[FIELD_PASSWORD].requestFocus();
                        AndroidUtilities.showKeyboard(inputFields[FIELD_PASSWORD]);
                    }
                }
            }, 200);
        }
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (chatAttachAlert != null) {
            chatAttachAlert.onPause();
        }
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didSetTwoStepPassword);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didRemovedTwoStepPassword);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didSetTwoStepPassword);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didRemovedTwoStepPassword);
        callCallback(false);
        if (chatAttachAlert != null) {
            chatAttachAlert.dismissInternal();
            chatAttachAlert.onDestroy();
        }
        if (currentActivityType == TYPE_PHONE_VERIFICATION) {
            for (int a = 0; a < views.length; a++) {
                if (views[a] != null) {
                    views[a].onDestroyActivity();
                }
            }
            if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                progressDialog = null;
            }
        }
        //AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid); TODO check
    }

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (checkDiscard()) {
                        return;
                    }
                    if (currentActivityType == TYPE_REQUEST || currentActivityType == TYPE_PASSWORD) {
                        callCallback(false);
                    }
                    finishFragment();
                } else if (id == info_item) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final TextView message = new TextView(getParentActivity());
                    Spannable spanned = new SpannableString(AndroidUtilities.replaceTags(LocaleController.getString("PassportInfo", R.string.PassportInfo)));
                    URLSpan[] spans = spanned.getSpans(0, spanned.length(), URLSpan.class);
                    for (int a = 0; a < spans.length; a++) {
                        URLSpan span = spans[a];
                        int start = spanned.getSpanStart(span);
                        int end = spanned.getSpanEnd(span);
                        spanned.removeSpan(span);
                        span = new URLSpanNoUnderline(span.getURL()) {
                            @Override
                            public void onClick(View widget) {
                                dismissCurrentDialig();
                                super.onClick(widget);
                            }
                        };
                        spanned.setSpan(span, start, end, 0);
                    }
                    message.setText(spanned);
                    message.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                    message.setLinkTextColor(Theme.getColor(Theme.key_dialogTextLink));
                    message.setHighlightColor(Theme.getColor(Theme.key_dialogLinkSelection));
                    message.setPadding(AndroidUtilities.dp(23), 0, AndroidUtilities.dp(23), 0);
                    //message.setMovementMethod(new SettingsActivity.LinkMovementMethodMy());
                    message.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));

                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setView(message);
                    builder.setTitle(LocaleController.getString("PassportInfoTitle", R.string.PassportInfoTitle));
                    builder.setNegativeButton(LocaleController.getString("Close", R.string.Close), null);
                    showDialog(builder.create());
                } else if (id == done_button) {
                    if (currentActivityType == TYPE_PASSWORD) {
                        onPasswordDone(false);
                        return;
                    }
                    final Runnable finishRunnable = new Runnable() {
                        @Override
                        public void run() {
                            finishFragment();
                        }
                    };
                    final ErrorRunnable errorRunnable = new ErrorRunnable() {
                        @Override
                        public void onError(String error, String text) {
                            if ("PHONE_VERIFICATION_NEEDED".equals(error)) {
                                startPhoneVerification(true, text, finishRunnable, this, delegate);
                            } else {
                                showEditDoneProgress(true, false);
                            }
                        }
                    };
                    if (currentActivityType == TYPE_EMAIL) {
                        String value;
                        if (useCurrentValue) {
                            value = currentEmail;
                        } else {
                            if (checkFieldsForError()) {
                                return;
                            }
                            value = inputFields[FIELD_EMAIL].getText().toString();
                        }
                        delegate.saveValue(currentType, value, null, null, null, null, null, null, null, finishRunnable, errorRunnable);
                    } else if (currentActivityType == TYPE_PHONE) {
                        String value;
                        if (useCurrentValue) {
                            value = UserConfig.getInstance(currentAccount).getCurrentUser().phone;
                        } else {
                            if (checkFieldsForError()) {
                                return;
                            }
                            value = inputFields[FIELD_PHONECODE].getText().toString() + inputFields[FIELD_PHONE].getText().toString();
                        }
                        delegate.saveValue(currentType, value, null, null, null, null, null, null, null, finishRunnable, errorRunnable);
                    } else if (currentActivityType == TYPE_ADDRESS) {
                        if (!uploadingDocuments.isEmpty() || checkFieldsForError()) {
                            return;
                        }
                        if (isHasNotAnyChanges()) {
                            finishFragment();
                            return;
                        }
                        JSONObject json = null;
                        try {
                            json = new JSONObject();
                            json.put("street_line1", inputFields[FIELD_STREET1].getText().toString());
                            json.put("street_line2", inputFields[FIELD_STREET2].getText().toString());
                            json.put("post_code", inputFields[FIELD_POSTCODE].getText().toString());
                            json.put("city", inputFields[FIELD_CITY].getText().toString());
                            json.put("state", inputFields[FIELD_STATE].getText().toString());
                            json.put("country_code", currentCitizeship);
                        } catch (Exception ignore) {

                        }
                        if (fieldsErrors != null) {
                            fieldsErrors.clear();
                        }
                        if (documentsErrors != null) {
                            documentsErrors.clear();
                        }
                        delegate.saveValue(currentType, null, json.toString(), currentDocumentsType, null, documents, selfieDocument, null, null, finishRunnable, errorRunnable);
                    } else if (currentActivityType == TYPE_IDENTITY) {
                        if (!uploadingDocuments.isEmpty() || checkFieldsForError()) {
                            return;
                        }
                        if (isHasNotAnyChanges()) {
                            finishFragment();
                            return;
                        }
                        JSONObject json = null;
                        JSONObject documentsJson = null;
                        try {
                            json = new JSONObject();
                            json.put("first_name", inputFields[FIELD_NAME].getText().toString());
                            json.put("last_name", inputFields[FIELD_SURNAME].getText().toString());
                            json.put("birth_date", inputFields[FIELD_BIRTHDAY].getText().toString());
                            json.put("gender", currentGender);
                            json.put("country_code", currentCitizeship);
                            json.put("residence_country_code", currentResidence);

                            if (currentDocumentsType != null) {
                                documentsJson = new JSONObject();
                                documentsJson.put("document_no", inputFields[FIELD_CARDNUMBER].getText().toString());
                                if (currentExpireDate[0] != 0) {
                                    documentsJson.put("expiry_date", String.format(Locale.US, "%02d.%02d.%d", currentExpireDate[2], currentExpireDate[1], currentExpireDate[0]));
                                } else {
                                    documentsJson.put("expiry_date", "");
                                }
                            }
                        } catch (Exception ignore) {

                        }
                        if (fieldsErrors != null) {
                            fieldsErrors.clear();
                        }
                        if (documentsErrors != null) {
                            documentsErrors.clear();
                        }
                        delegate.saveValue(currentType, null, json.toString(), currentDocumentsType, documentsJson != null ? documentsJson.toString() : null, null, selfieDocument, frontDocument, reverseLayout != null && reverseLayout.getVisibility() == View.VISIBLE ? reverseDocument : null, finishRunnable, errorRunnable);
                    } else if (currentActivityType == TYPE_EMAIL_VERIFICATION) {
                        final TLRPC.TL_account_verifyEmail req = new TLRPC.TL_account_verifyEmail();
                        req.email = currentValues.get("email");
                        req.code = inputFields[FIELD_EMAIL].getText().toString();
                        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                            @Override
                            public void run(final TLObject response, final TLRPC.TL_error error) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (error == null) {
                                            delegate.saveValue(currentType, currentValues.get("email"), null, null, null, null, null, null, null, finishRunnable, errorRunnable);
                                        } else {
                                            AlertsCreator.processError(currentAccount, error, PassportActivity.this, req);
                                            errorRunnable.onError(null, null);
                                        }
                                    }
                                });
                            }
                        });
                        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
                    } else if (currentActivityType == TYPE_PHONE_VERIFICATION) {
                        views[currentViewNum].onNextPressed();
                    }
                    showEditDoneProgress(true, true);
                }
            }
        });

        if (currentActivityType == TYPE_PHONE_VERIFICATION) {
            fragmentView = scrollView = new ScrollView(context) {
                @Override
                protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                    return false;
                }
            };
            scrollView.setFillViewport(true);
            AndroidUtilities.setScrollViewEdgeEffectColor(scrollView, Theme.getColor(Theme.key_actionBarDefault));
        } else {
            fragmentView = new FrameLayout(context);
            FrameLayout frameLayout = (FrameLayout) fragmentView;
            fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

            scrollView = new ScrollView(context) {
                @Override
                protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                    return false;
                }

                @Override
                public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
                    rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
                    rectangle.top += AndroidUtilities.dp(20);
                    rectangle.bottom += AndroidUtilities.dp(50);
                    return super.requestChildRectangleOnScreen(child, rectangle, immediate);
                }
            };
            scrollView.setFillViewport(true);
            AndroidUtilities.setScrollViewEdgeEffectColor(scrollView, Theme.getColor(Theme.key_actionBarDefault));
            frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, currentActivityType == TYPE_REQUEST ? 48 : 0));

            linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(linearLayout2, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        if (currentActivityType != TYPE_REQUEST && currentActivityType != TYPE_MANAGE) {
            ActionBarMenu menu = actionBar.createMenu();
            doneItem = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));
            progressView = new ContextProgressView(context, 1);
            doneItem.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            progressView.setVisibility(View.INVISIBLE);

            if (currentActivityType == TYPE_IDENTITY || currentActivityType == TYPE_ADDRESS) {
                if (chatAttachAlert != null) {
                    try {
                        if (chatAttachAlert.isShowing()) {
                            chatAttachAlert.dismiss();
                        }
                    } catch (Exception ignore) {

                    }
                    chatAttachAlert.onDestroy();
                    chatAttachAlert = null;
                }
            }
        }

        if (currentActivityType == TYPE_PASSWORD) {
            createPasswordInterface(context);
        } else if (currentActivityType == TYPE_REQUEST) {
            createRequestInterface(context);
        } else if (currentActivityType == TYPE_IDENTITY) {
            createIdentityInterface(context);
            fillInitialValues();
        } else if (currentActivityType == TYPE_ADDRESS) {
            createAddressInterface(context);
            fillInitialValues();
        } else if (currentActivityType == TYPE_PHONE) {
            createPhoneInterface(context);
        } else if (currentActivityType == TYPE_EMAIL) {
            createEmailInterface(context);
        } else if (currentActivityType == TYPE_EMAIL_VERIFICATION) {
            createEmailVerificationInterface(context);
        } else if (currentActivityType == TYPE_PHONE_VERIFICATION) {
            createPhoneVerificationInterface(context);
        } else if (currentActivityType == TYPE_MANAGE) {
            createManageInterface(context);
        }
        return fragmentView;
    }

    @Override
    public boolean dismissDialogOnPause(Dialog dialog) {
        return dialog != chatAttachAlert && super.dismissDialogOnPause(dialog);
    }

    @Override
    public void dismissCurrentDialig() {
        if (chatAttachAlert != null && visibleDialog == chatAttachAlert) {
            chatAttachAlert.closeCamera(false);
            chatAttachAlert.dismissInternal();
            chatAttachAlert.hideCamera(true);
            return;
        }
        super.dismissCurrentDialig();
    }

    private void createPhoneVerificationInterface(Context context) {
        actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));

        FrameLayout frameLayout = new FrameLayout(context);
        scrollView.addView(frameLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT));

        for (int a = 0; a < 3; a++) {
            views[a] = new PhoneConfirmationView(context, a + 2);
            views[a].setVisibility(View.GONE);
            frameLayout.addView(views[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, AndroidUtilities.isTablet() ? 26 : 18, 30, AndroidUtilities.isTablet() ? 26 : 18, 0));
        }
        final Bundle params = new Bundle();
        params.putString("phone", currentValues.get("phone"));
        fillNextCodeParams(params, currentPhoneVerification, false);
    }

    private void loadPasswordInfo() {
        TLRPC.TL_account_getPassword req = new TLRPC.TL_account_getPassword();
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response != null) {
                            currentPassword = (TLRPC.account_Password) response;

                            byte[] salt = new byte[currentPassword.new_salt.length + 8];
                            Utilities.random.nextBytes(salt);
                            System.arraycopy(currentPassword.new_salt, 0, salt, 0, currentPassword.new_salt.length);
                            currentPassword.new_salt = salt;

                            updatePasswordInterface();

                            if (inputFieldContainers[FIELD_PASSWORD].getVisibility() == View.VISIBLE) {
                                inputFields[FIELD_PASSWORD].requestFocus();
                                AndroidUtilities.showKeyboard(inputFields[FIELD_PASSWORD]);
                            }
                        }
                    }
                });
            }
        });
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    private void createEmailVerificationInterface(Context context) {
        actionBar.setTitle(LocaleController.getString("PassportEmail", R.string.PassportEmail));

        inputFields = new EditTextBoldCursor[1];
        for (int a = 0; a < 1; a++) {
            ViewGroup container = new FrameLayout(context);
            linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            inputFields[a] = new EditTextBoldCursor(context);
            inputFields[a].setTag(a);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            inputFields[a].setInputType(InputType.TYPE_CLASS_PHONE);

            inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            switch (a) {
                case FIELD_EMAIL:
                    inputFields[a].setHint(LocaleController.getString("PassportEmailCode", R.string.PassportEmailCode));
                    break;
            }
            inputFields[a].setSelection(inputFields[a].length());
            inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
            inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 12, 17, 6));

            inputFields[a].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT) {
                        doneItem.callOnClick();
                        return true;
                    }
                    return false;
                }
            });

            inputFields[a].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (ignoreOnTextChange) {
                        return;
                    }
                    if (emailCodeLength != 0 && inputFields[FIELD_EMAIL].length() == emailCodeLength) {
                        doneItem.callOnClick();
                    }
                }
            });
        }

        bottomCell = new TextInfoPrivacyCell(context);
        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        bottomCell.setText(LocaleController.formatString("PassportEmailVerifyInfo", R.string.PassportEmailVerifyInfo, currentValues.get("email")));
        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void createPasswordInterface(Context context) {
        TLRPC.User botUser = null;
        if (currentForm != null) {
            for (int a = 0; a < currentForm.users.size(); a++) {
                TLRPC.User user = currentForm.users.get(a);
                if (user.id == currentBotId) {
                    botUser = user;
                    break;
                }
            }
        } else {
            botUser = UserConfig.getInstance(currentAccount).getCurrentUser();
        }

        FrameLayout frameLayout = (FrameLayout) fragmentView;

        actionBar.setTitle(LocaleController.getString("TelegramPassport", R.string.TelegramPassport));

        emptyView = new EmptyTextProgressView(context);
        emptyView.showProgress();
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        passwordAvatarContainer = new FrameLayout(context);
        linearLayout2.addView(passwordAvatarContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 100));

        BackupImageView avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(32));
        passwordAvatarContainer.addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.CENTER, 0, 8, 0, 0));

        AvatarDrawable avatarDrawable = new AvatarDrawable(botUser);
        TLRPC.FileLocation photo = null;
        if (botUser.photo != null) {
            photo = botUser.photo.photo_small;
        }
        avatarImageView.setImage(photo, "50_50", avatarDrawable);

        passwordRequestTextView = new TextInfoPrivacyCell(context);
        passwordRequestTextView.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
        if (currentBotId == 0) {
            passwordRequestTextView.setText(LocaleController.getString("PassportSelfRequest", R.string.PassportSelfRequest));
        } else {
            passwordRequestTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("PassportRequest", R.string.PassportRequest, UserObject.getFirstName(botUser))));
        }
        ((FrameLayout.LayoutParams) passwordRequestTextView.getTextView().getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
        linearLayout2.addView(passwordRequestTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 17, 0, 17, 0));

        noPasswordImageView = new ImageView(context);
        noPasswordImageView.setImageResource(R.drawable.no_password);
        noPasswordImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_messagePanelIcons), PorterDuff.Mode.MULTIPLY));
        linearLayout2.addView(noPasswordImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 13, 0, 0));

        noPasswordTextView = new TextView(context);
        noPasswordTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        noPasswordTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        noPasswordTextView.setPadding(AndroidUtilities.dp(17), AndroidUtilities.dp(10), AndroidUtilities.dp(17), AndroidUtilities.dp(17));
        noPasswordTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
        noPasswordTextView.setText(LocaleController.getString("TelegramPassportCreatePasswordInfo", R.string.TelegramPassportCreatePasswordInfo));
        linearLayout2.addView(noPasswordTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 17, 10, 17, 0));

        noPasswordSetTextView = new TextView(context);
        noPasswordSetTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText5));
        noPasswordSetTextView.setGravity(Gravity.CENTER);
        noPasswordSetTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        noPasswordSetTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        noPasswordSetTextView.setText(LocaleController.getString("TelegramPassportCreatePassword", R.string.TelegramPassportCreatePassword));
        linearLayout2.addView(noPasswordSetTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 17, 9, 17, 0));
        noPasswordSetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwoStepVerificationActivity activity = new TwoStepVerificationActivity(currentAccount, 1);
                activity.setCloseAfterSet(true);
                activity.setCurrentPasswordInfo(new byte[0], currentPassword);
                presentFragment(activity);
            }
        });

        inputFields = new EditTextBoldCursor[1];
        inputFieldContainers = new ViewGroup[1];
        for (int a = 0; a < 1; a++) {
            inputFieldContainers[a] = new FrameLayout(context);
            linearLayout2.addView(inputFieldContainers[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            inputFieldContainers[a].setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            inputFields[a] = new EditTextBoldCursor(context);
            inputFields[a].setTag(a);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            inputFields[a].setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            inputFields[a].setMaxLines(1);
            inputFields[a].setLines(1);
            inputFields[a].setSingleLine(true);
            inputFields[a].setTransformationMethod(PasswordTransformationMethod.getInstance());
            inputFields[a].setTypeface(Typeface.DEFAULT);
            inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
            inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            inputFieldContainers[a].addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 12, 17, 6));

            inputFields[a].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                        doneItem.callOnClick();
                        return true;
                    }
                    return false;
                }
            });
            inputFields[a].setCustomSelectionActionModeCallback(new ActionMode.Callback() {
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    return false;
                }
            });
        }

        passwordInfoRequestTextView = new TextInfoPrivacyCell(context);
        passwordInfoRequestTextView.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        passwordInfoRequestTextView.setText(LocaleController.formatString("PassportRequestPasswordInfo", R.string.PassportRequestPasswordInfo));
        linearLayout2.addView(passwordInfoRequestTextView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        passwordForgotButton = new TextView(context);
        passwordForgotButton.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        passwordForgotButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        passwordForgotButton.setText(LocaleController.getString("ForgotPassword", R.string.ForgotPassword));
        passwordForgotButton.setPadding(0, 0, 0, 0);
        linearLayout2.addView(passwordForgotButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 30, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 17, 0, 17, 0));
        passwordForgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPassword.has_recovery) {
                    needShowProgress();
                    TLRPC.TL_auth_requestPasswordRecovery req = new TLRPC.TL_auth_requestPasswordRecovery();
                    int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                        @Override
                        public void run(final TLObject response, final TLRPC.TL_error error) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    needHideProgress();
                                    if (error == null) {
                                        final TLRPC.TL_auth_passwordRecovery res = (TLRPC.TL_auth_passwordRecovery) response;
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                        builder.setMessage(LocaleController.formatString("RestoreEmailSent", R.string.RestoreEmailSent, res.email_pattern));
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                TwoStepVerificationActivity fragment = new TwoStepVerificationActivity(currentAccount, 1);
                                                fragment.setRecoveryParams(currentPassword);
                                                currentPassword.email_unconfirmed_pattern = res.email_pattern;
                                                presentFragment(fragment);
                                            }
                                        });
                                        Dialog dialog = showDialog(builder.create());
                                        if (dialog != null) {
                                            dialog.setCanceledOnTouchOutside(false);
                                            dialog.setCancelable(false);
                                        }
                                    } else {
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            int time = Utilities.parseInt(error.text);
                                            String timeString;
                                            if (time < 60) {
                                                timeString = LocaleController.formatPluralString("Seconds", time);
                                            } else {
                                                timeString = LocaleController.formatPluralString("Minutes", time / 60);
                                            }
                                            showAlertWithText(LocaleController.getString("AppName", R.string.AppName), LocaleController.formatString("FloodWaitTime", R.string.FloodWaitTime, timeString));
                                        } else {
                                            showAlertWithText(LocaleController.getString("AppName", R.string.AppName), error.text);
                                        }
                                    }
                                }
                            });
                        }
                    }, ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
                    ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
                } else {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                    builder.setNegativeButton(LocaleController.getString("RestorePasswordResetAccount", R.string.RestorePasswordResetAccount), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Browser.openUrl(getParentActivity(), "https://telegram.org/deactivate?phone=" + UserConfig.getInstance(currentAccount).getClientPhone());
                        }
                    });
                    builder.setTitle(LocaleController.getString("RestorePasswordNoEmailTitle", R.string.RestorePasswordNoEmailTitle));
                    builder.setMessage(LocaleController.getString("RestorePasswordNoEmailText", R.string.RestorePasswordNoEmailText));
                    showDialog(builder.create());
                }
            }
        });

        updatePasswordInterface();
    }

    private void onPasswordDone(final boolean saved) {
        final byte[] currentPasswordHash;
        final String textPassword;
        if (saved) {
            currentPasswordHash = savedPasswordHash;
            textPassword = null;
        } else {
            textPassword = inputFields[FIELD_PASSWORD].getText().toString();
            if (TextUtils.isEmpty(textPassword)) {
                onPasscodeError(false);
                return;
            }
            showEditDoneProgress(true, true);
            byte[] passwordBytes = AndroidUtilities.getStringBytes(textPassword);
            byte[] hash = new byte[currentPassword.current_salt.length * 2 + passwordBytes.length];
            System.arraycopy(currentPassword.current_salt, 0, hash, 0, currentPassword.current_salt.length);
            System.arraycopy(passwordBytes, 0, hash, currentPassword.current_salt.length, passwordBytes.length);
            System.arraycopy(currentPassword.current_salt, 0, hash, hash.length - currentPassword.current_salt.length, currentPassword.current_salt.length);
            currentPasswordHash = Utilities.computeSHA256(hash, 0, hash.length);
        }

        RequestDelegate requestDelegate = new RequestDelegate() {

            private void openRequestInterface() {
                if (inputFields == null) {
                    return;
                }
                if (!saved) {
                    UserConfig.getInstance(currentAccount).savePassword(currentPasswordHash, saltedPassword);
                }

                AndroidUtilities.hideKeyboard(inputFields[FIELD_PASSWORD]);
                ignoreOnFailure = true;
                int type;
                if (currentBotId == 0) {
                    type = TYPE_MANAGE;
                } else {
                    type = TYPE_REQUEST;
                }
                PassportActivity activity = new PassportActivity(type, currentBotId, currentScope, currentPublicKey, currentPayload, currentCallbackUrl, currentForm, currentPassword);
                activity.currentEmail = currentEmail;
                activity.currentAccount = currentAccount;
                activity.saltedPassword = saltedPassword;
                activity.secureSecret = secureSecret;
                activity.secureSecretId = secureSecretId;
                activity.needActivityResult = needActivityResult;
                if (parentLayout == null || !parentLayout.checkTransitionAnimation()) {
                    presentFragment(activity, true);
                } else {
                    presentAfterAnimation = activity;
                }
            }

            private void resetSecret() {
                TLRPC.TL_account_updatePasswordSettings req = new TLRPC.TL_account_updatePasswordSettings();
                req.current_password_hash = currentPasswordHash;
                req.new_settings = new TLRPC.TL_account_passwordInputSettings();
                req.new_settings.new_secure_secret = new byte[0];
                req.new_settings.new_secure_salt = new byte[0];
                req.new_settings.new_secure_secret_id = 0;
                req.new_settings.flags |= 4;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                generateNewSecret();
                            }
                        });
                    }
                });
            }

            private void generateNewSecret() {
                Utilities.random.setSeed(currentPassword.secure_random);
                byte[] secureSalt = new byte[currentPassword.new_secure_salt.length + 8];
                Utilities.random.nextBytes(secureSalt);
                System.arraycopy(currentPassword.new_secure_salt, 0, secureSalt, 0, currentPassword.new_secure_salt.length);

                saltedPassword = Utilities.computeSHA512(secureSalt, AndroidUtilities.getStringBytes(textPassword), secureSalt);
                byte[] key = new byte[32];
                System.arraycopy(saltedPassword, 0, key, 0, 32);
                byte[] iv = new byte[16];
                System.arraycopy(saltedPassword, 32, iv, 0, 16);

                secureSecret = getRandomSecret();
                secureSecretId = Utilities.bytesToLong(Utilities.computeSHA256(secureSecret));
                Utilities.aesCbcEncryptionByteArraySafe(secureSecret, key, iv, 0, secureSecret.length, 0, 1);

                TLRPC.TL_account_updatePasswordSettings req = new TLRPC.TL_account_updatePasswordSettings();
                req.current_password_hash = currentPasswordHash;
                req.new_settings = new TLRPC.TL_account_passwordInputSettings();
                req.new_settings.new_secure_secret = secureSecret;
                req.new_settings.new_secure_salt = secureSalt;
                req.new_settings.new_secure_secret_id = secureSecretId;
                req.new_settings.flags |= 4;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (currentForm == null) {
                                    currentForm = new TLRPC.TL_account_authorizationForm();
                                    currentForm.selfie_required = true;
                                }
                                openRequestInterface();
                            }
                        });
                    }
                });
            }

            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_account_passwordSettings settings = (TLRPC.TL_account_passwordSettings) response;
                            secureSecret = settings.secure_secret;
                            secureSecretId = settings.secure_secret_id;
                            currentEmail = settings.email;
                            if (saved) {
                                saltedPassword = savedSaltedPassword;
                            } else {
                                saltedPassword = Utilities.computeSHA512(settings.secure_salt, AndroidUtilities.getStringBytes(textPassword), settings.secure_salt);
                            }

                            if (!checkSecret(decryptSecret(secureSecret, saltedPassword), secureSecretId) || settings.secure_salt.length == 0 || secureSecretId == 0) {
                                if (saved) {
                                    UserConfig.getInstance(currentAccount).resetSavedPassword();
                                    usingSavedPassword = 0;
                                    updatePasswordInterface();
                                } else {
                                    if (secureSecret == null || secureSecret.length == 0) {
                                        generateNewSecret();
                                    } else {
                                        resetSecret();
                                    }
                                }
                            } else if (currentBotId == 0) {
                                TLRPC.TL_account_getAllSecureValues req = new TLRPC.TL_account_getAllSecureValues();
                                ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                                    @Override
                                    public void run(final TLObject response, final TLRPC.TL_error error) {
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (response != null) {
                                                    currentForm = new TLRPC.TL_account_authorizationForm();
                                                    currentForm.selfie_required = true;
                                                    TLRPC.Vector vector = (TLRPC.Vector) response;
                                                    for (int a = 0, size = vector.objects.size(); a < size; a++) {
                                                        currentForm.values.add((TLRPC.TL_secureValue) vector.objects.get(a));
                                                    }
                                                    openRequestInterface();
                                                } else {
                                                    showAlertWithText(LocaleController.getString("AppName", R.string.AppName), error.text);
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                openRequestInterface();
                            }
                        } else {
                            if (saved) {
                                UserConfig.getInstance(currentAccount).resetSavedPassword();
                                usingSavedPassword = 0;
                                updatePasswordInterface();
                                if (inputFieldContainers[FIELD_PASSWORD].getVisibility() == View.VISIBLE) {
                                    inputFields[FIELD_PASSWORD].requestFocus();
                                    AndroidUtilities.showKeyboard(inputFields[FIELD_PASSWORD]);
                                }
                            } else {
                                showEditDoneProgress(true, false);
                                if (error.text.equals("PASSWORD_HASH_INVALID")) {
                                    onPasscodeError(true);
                                } else if (error.text.startsWith("FLOOD_WAIT")) {
                                    int time = Utilities.parseInt(error.text);
                                    String timeString;
                                    if (time < 60) {
                                        timeString = LocaleController.formatPluralString("Seconds", time);
                                    } else {
                                        timeString = LocaleController.formatPluralString("Minutes", time / 60);
                                    }
                                    showAlertWithText(LocaleController.getString("AppName", R.string.AppName), LocaleController.formatString("FloodWaitTime", R.string.FloodWaitTime, timeString));
                                } else {
                                    showAlertWithText(LocaleController.getString("AppName", R.string.AppName), error.text);
                                }
                            }
                        }
                    }
                });
            }
        };

        TLRPC.TL_account_getPasswordSettings req = new TLRPC.TL_account_getPasswordSettings();
        req.current_password_hash = currentPasswordHash;
        int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, requestDelegate, ConnectionsManager.RequestFlagFailOnServerErrors | ConnectionsManager.RequestFlagWithoutLogin);
        ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
    }

    private void createRequestInterface(Context context) {
        TLRPC.User botUser = null;
        if (currentForm != null) {
            for (int a = 0; a < currentForm.users.size(); a++) {
                TLRPC.User user = currentForm.users.get(a);
                if (user.id == currentBotId) {
                    botUser = user;
                    break;
                }
            }
        }

        FrameLayout frameLayout = (FrameLayout) fragmentView;

        actionBar.setTitle(LocaleController.getString("TelegramPassport", R.string.TelegramPassport));

        actionBar.createMenu().addItem(info_item, R.drawable.profile_info);

        if (botUser != null) {
            FrameLayout avatarContainer = new FrameLayout(context);
            linearLayout2.addView(avatarContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 100));

            BackupImageView avatarImageView = new BackupImageView(context);
            avatarImageView.setRoundRadius(AndroidUtilities.dp(32));
            avatarContainer.addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.CENTER, 0, 8, 0, 0));

            AvatarDrawable avatarDrawable = new AvatarDrawable(botUser);
            TLRPC.FileLocation photo = null;
            if (botUser.photo != null) {
                photo = botUser.photo.photo_small;
            }
            avatarImageView.setImage(photo, "50_50", avatarDrawable);

            bottomCell = new TextInfoPrivacyCell(context);
            bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
            bottomCell.setText(AndroidUtilities.replaceTags(LocaleController.formatString("PassportRequest", R.string.PassportRequest, UserObject.getFirstName(botUser))));
            bottomCell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
            ((FrameLayout.LayoutParams) bottomCell.getTextView().getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
            linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString("PassportRequestedInformation", R.string.PassportRequestedInformation));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (currentForm != null) {
            for (int a = 0, size = currentForm.required_types.size(); a < size; a++) {
                TLRPC.SecureValueType type = currentForm.required_types.get(a);
                ArrayList<TLRPC.SecureValueType> documentTypes;
                if (type instanceof TLRPC.TL_secureValueTypePhone || type instanceof TLRPC.TL_secureValueTypeEmail) {
                    documentTypes = null;
                } else if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                    documentTypes = new ArrayList<>();
                    for (int b = 0; b < size; b++) {
                        TLRPC.SecureValueType innerType = currentForm.required_types.get(b);
                        if (innerType instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                                innerType instanceof TLRPC.TL_secureValueTypePassport ||
                                innerType instanceof TLRPC.TL_secureValueTypeInternalPassport ||
                                innerType instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                            documentTypes.add(innerType);
                        }
                    }
                } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
                    documentTypes = new ArrayList<>();
                    for (int b = 0; b < size; b++) {
                        TLRPC.SecureValueType innerType = currentForm.required_types.get(b);
                        if (innerType instanceof TLRPC.TL_secureValueTypeUtilityBill ||
                                innerType instanceof TLRPC.TL_secureValueTypeBankStatement ||
                                innerType instanceof TLRPC.TL_secureValueTypePassportRegistration ||
                                innerType instanceof TLRPC.TL_secureValueTypeTemporaryRegistration ||
                                innerType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                            documentTypes.add(innerType);
                        }
                    }
                } else {
                    continue;
                }
                addField(context, currentForm.required_types.get(a), documentTypes, a == size - 1);
            }
        }

        if (botUser != null) {
            bottomCell = new TextInfoPrivacyCell(context);
            bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            bottomCell.setLinkTextColorKey(Theme.key_windowBackgroundWhiteGrayText4);
            if (!TextUtils.isEmpty(currentForm.privacy_policy_url)) {
                String str2 = LocaleController.formatString("PassportPolicy", R.string.PassportPolicy, UserObject.getFirstName(botUser), botUser.username);
                SpannableStringBuilder text = new SpannableStringBuilder(str2);
                int index1 = str2.indexOf('*');
                int index2 = str2.lastIndexOf('*');
                if (index1 != -1 && index2 != -1) {
                    bottomCell.getTextView().setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
                    text.replace(index2, index2 + 1, "");
                    text.replace(index1, index1 + 1, "");
                    text.setSpan(new LinkSpan(), index1, index2 - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                bottomCell.setText(text);
            } else {
                bottomCell.setText(AndroidUtilities.replaceTags(LocaleController.formatString("PassportNoPolicy", R.string.PassportNoPolicy, UserObject.getFirstName(botUser), botUser.username)));
            }
            bottomCell.getTextView().setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText4));
            bottomCell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
            linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        bottomLayout = new FrameLayout(context);
        bottomLayout.setBackgroundDrawable(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
        frameLayout.addView(bottomLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));
        bottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<TLRPC.TL_secureValue> valuesToSend = new ArrayList<>();
                for (int a = 0, size = currentForm.required_types.size(); a < size; a++) {
                    TLRPC.SecureValueType type = currentForm.required_types.get(a);
                    TLRPC.SecureValueType innerType = null;
                    boolean needDocuments = false;
                    TLRPC.TL_secureValue documentValue = null;
                    if (type instanceof TLRPC.TL_secureValueTypePhone || type instanceof TLRPC.TL_secureValueTypeEmail) {
                        needDocuments = false;
                    } else if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                        for (int b = 0; b < size; b++) {
                            innerType = currentForm.required_types.get(b);
                            if (innerType instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                                    innerType instanceof TLRPC.TL_secureValueTypePassport ||
                                    innerType instanceof TLRPC.TL_secureValueTypeInternalPassport ||
                                    innerType instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                                needDocuments = true;
                                documentValue = getValueByType(innerType, true);
                                if (documentValue != null) {
                                    break;
                                }
                            }
                        }
                    } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
                        for (int b = 0; b < size; b++) {
                            innerType = currentForm.required_types.get(b);
                            if (innerType instanceof TLRPC.TL_secureValueTypeUtilityBill ||
                                    innerType instanceof TLRPC.TL_secureValueTypeBankStatement ||
                                    innerType instanceof TLRPC.TL_secureValueTypePassportRegistration ||
                                    innerType instanceof TLRPC.TL_secureValueTypeTemporaryRegistration ||
                                    innerType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                                needDocuments = true;
                                documentValue = getValueByType(innerType, true);
                                if (documentValue != null) {
                                    break;
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                    TLRPC.TL_secureValue value = getValueByType(type, true);
                    if (value == null || documentValue == null && needDocuments) {
                        Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(typesViews.get(type), 2, 0);
                        return;
                    }
                    String key = getNameForType(type);
                    String key2 = innerType != null ? getNameForType(innerType) : null;
                    HashMap<String, String> errors = errorsMap.get(key);
                    HashMap<String, String> errors2 = key2 != null ? errorsMap.get(key2) : null;
                    if (errors != null && !errors.isEmpty() || errors2 != null && !errors2.isEmpty()) {
                        Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if (v != null) {
                            v.vibrate(200);
                        }
                        AndroidUtilities.shakeView(typesViews.get(type), 2, 0);
                        return;
                    }
                    valuesToSend.add(value);
                    if (documentValue != null) {
                        valuesToSend.add(documentValue);
                    }
                }
                showEditDoneProgress(false, true);
                TLRPC.TL_account_acceptAuthorization req = new TLRPC.TL_account_acceptAuthorization();
                req.bot_id = currentBotId;
                req.scope = currentScope;
                req.public_key = currentPublicKey;
                JSONObject jsonObject = new JSONObject();
                for (int a = 0, size = valuesToSend.size(); a < size; a++) {
                    TLRPC.TL_secureValue secureValue = valuesToSend.get(a);

                    JSONObject data = new JSONObject();

                    if (secureValue.plain_data != null) {
                        if (secureValue.plain_data instanceof TLRPC.TL_securePlainEmail) {
                            TLRPC.TL_securePlainEmail securePlainEmail = (TLRPC.TL_securePlainEmail) secureValue.plain_data;
                        } else if (secureValue.plain_data instanceof TLRPC.TL_securePlainPhone) {
                            TLRPC.TL_securePlainPhone securePlainPhone = (TLRPC.TL_securePlainPhone) secureValue.plain_data;
                        }
                    } else {
                        try {
                            JSONObject result = new JSONObject();
                            if (secureValue.data != null) {
                                byte[] decryptedSecret = decryptValueSecret(secureValue.data.secret, secureValue.data.data_hash);

                                data.put("data_hash", Base64.encodeToString(secureValue.data.data_hash, Base64.NO_WRAP));
                                data.put("secret", Base64.encodeToString(decryptedSecret, Base64.NO_WRAP));

                                result.put("data", data);
                            }
                            if (!secureValue.files.isEmpty()) {
                                JSONArray files = new JSONArray();
                                for (int b = 0, size2 = secureValue.files.size(); b < size2; b++) {
                                    TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) secureValue.files.get(b);
                                    byte[] decryptedSecret = decryptValueSecret(secureFile.secret, secureFile.file_hash);

                                    JSONObject file = new JSONObject();
                                    file.put("file_hash", Base64.encodeToString(secureFile.file_hash, Base64.NO_WRAP));
                                    file.put("secret", Base64.encodeToString(decryptedSecret, Base64.NO_WRAP));
                                    files.put(file);
                                }
                                result.put("files", files);
                            }
                            if (secureValue.front_side instanceof TLRPC.TL_secureFile) {
                                TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) secureValue.front_side;
                                byte[] decryptedSecret = decryptValueSecret(secureFile.secret, secureFile.file_hash);

                                JSONObject front = new JSONObject();
                                front.put("file_hash", Base64.encodeToString(secureFile.file_hash, Base64.NO_WRAP));
                                front.put("secret", Base64.encodeToString(decryptedSecret, Base64.NO_WRAP));
                                result.put("front_side", front);
                            }
                            if (secureValue.reverse_side instanceof TLRPC.TL_secureFile) {
                                TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) secureValue.reverse_side;
                                byte[] decryptedSecret = decryptValueSecret(secureFile.secret, secureFile.file_hash);

                                JSONObject reverse = new JSONObject();
                                reverse.put("file_hash", Base64.encodeToString(secureFile.file_hash, Base64.NO_WRAP));
                                reverse.put("secret", Base64.encodeToString(decryptedSecret, Base64.NO_WRAP));
                                result.put("reverse_side", reverse);
                            }
                            if (currentForm.selfie_required && secureValue.selfie instanceof TLRPC.TL_secureFile) {
                                TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) secureValue.selfie;
                                byte[] decryptedSecret = decryptValueSecret(secureFile.secret, secureFile.file_hash);

                                JSONObject selfie = new JSONObject();
                                selfie.put("file_hash", Base64.encodeToString(secureFile.file_hash, Base64.NO_WRAP));
                                selfie.put("secret", Base64.encodeToString(decryptedSecret, Base64.NO_WRAP));
                                result.put("selfie", selfie);
                            }
                            jsonObject.put(getNameForType(secureValue.type), result);
                        } catch (Exception ignore) {

                        }
                    }

                    TLRPC.TL_secureValueHash hash = new TLRPC.TL_secureValueHash();
                    hash.type = secureValue.type;
                    hash.hash = secureValue.hash;
                    req.value_hashes.add(hash);
                }
                JSONObject result = new JSONObject();
                try {
                    result.put("secure_data", jsonObject);
                } catch (Exception ignore) {

                }
                if (currentPayload != null) {
                    try {
                        result.put("payload", currentPayload);
                    } catch (Exception ignore) {

                    }
                }
                String json = result.toString();

                EncryptionResult encryptionResult = encryptData(AndroidUtilities.getStringBytes(json));

                req.credentials = new TLRPC.TL_secureCredentialsEncrypted();
                req.credentials.hash = encryptionResult.fileHash;
                req.credentials.data = encryptionResult.encryptedData;
                try {
                    String key = currentPublicKey.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT));
                    RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

                    Cipher c = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC");
                    c.init(Cipher.ENCRYPT_MODE, pubKey);
                    req.credentials.secret = c.doFinal(encryptionResult.decrypyedFileSecret);
                } catch (Exception e) {
                    FileLog.e(e);
                }
                int reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(TLObject response, final TLRPC.TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (error == null) {
                                    ignoreOnFailure = true;
                                    callCallback(true);
                                    finishFragment();
                                } else {
                                    showEditDoneProgress(false, false);
                                    if ("APP_VERSION_OUTDATED".equals(error.text)) {
                                        AlertsCreator.showUpdateAppAlert(getParentActivity(), LocaleController.getString("UpdateAppAlert", R.string.UpdateAppAlert), true);
                                    } else {
                                        showAlertWithText(LocaleController.getString("AppName", R.string.AppName), error.text);
                                    }
                                }
                            }
                        });
                    }
                });
                ConnectionsManager.getInstance(currentAccount).bindRequestToGuid(reqId, classGuid);
            }
        });

        acceptTextView = new TextView(context);
        acceptTextView.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        acceptTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.authorize, 0, 0, 0);
        acceptTextView.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        acceptTextView.setText(LocaleController.getString("PassportAuthorize", R.string.PassportAuthorize));
        acceptTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        acceptTextView.setGravity(Gravity.CENTER);
        acceptTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        bottomLayout.addView(acceptTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

        progressViewButton = new ContextProgressView(context, 0);
        progressViewButton.setVisibility(View.INVISIBLE);
        bottomLayout.addView(progressViewButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        View shadow = new View(context);
        shadow.setBackgroundResource(R.drawable.header_shadow_reverse);
        frameLayout.addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3, Gravity.LEFT | Gravity.BOTTOM, 0, 0, 0, 48));
    }

    private void createManageInterface(Context context) {
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        actionBar.setTitle(LocaleController.getString("TelegramPassport", R.string.TelegramPassport));

        actionBar.createMenu().addItem(info_item, R.drawable.profile_info);

        headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString("PassportProvidedInformation", R.string.PassportProvidedInformation));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        sectionCell = new ShadowSectionCell(context);
        sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        linearLayout2.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        addDocumentCell = new TextSettingsCell(context);
        addDocumentCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
        addDocumentCell.setText(LocaleController.getString("PassportNoDocumentsAdd", R.string.PassportNoDocumentsAdd), true);
        linearLayout2.addView(addDocumentCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        addDocumentCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddDocumentAlert();
            }
        });

        deletePassportCell = new TextSettingsCell(context);
        deletePassportCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText3));
        deletePassportCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
        deletePassportCell.setText(LocaleController.getString("TelegramPassportDelete", R.string.TelegramPassportDelete), false);
        linearLayout2.addView(deletePassportCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        deletePassportCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TLRPC.TL_account_deleteSecureValue req = new TLRPC.TL_account_deleteSecureValue();
                        for (int a = 0; a < currentForm.values.size(); a++) {
                            req.types.add(currentForm.values.get(a).type);
                        }
                        needShowProgress();
                        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                            @Override
                            public void run(TLObject response, TLRPC.TL_error error) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int a = 0; a < linearLayout2.getChildCount(); a++) {
                                            View child = linearLayout2.getChildAt(a);
                                            if (child instanceof TextDetailSecureCell) {
                                                linearLayout2.removeView(child);
                                                a--;
                                            }
                                        }
                                        needHideProgress();
                                        typesViews.clear();
                                        typesValues.clear();
                                        currentForm.values.clear();
                                        updateManageVisibility();
                                    }
                                });
                            }
                        });
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("TelegramPassportDeleteAlert", R.string.TelegramPassportDeleteAlert));
                showDialog(builder.create());
            }
        });

        addDocumentSectionCell = new ShadowSectionCell(context);
        addDocumentSectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        linearLayout2.addView(addDocumentSectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        emptyLayout = new LinearLayout(context);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(Gravity.CENTER);
        emptyLayout.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        linearLayout2.addView(emptyLayout, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.displaySize.y - ActionBar.getCurrentActionBarHeight()));

        emptyImageView = new ImageView(context);
        emptyImageView.setImageResource(R.drawable.no_passport);
        emptyImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_sessions_devicesImage), PorterDuff.Mode.MULTIPLY));
        emptyLayout.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        emptyTextView1 = new TextView(context);
        emptyTextView1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyTextView1.setGravity(Gravity.CENTER);
        emptyTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        emptyTextView1.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        emptyTextView1.setText(LocaleController.getString("PassportNoDocuments", R.string.PassportNoDocuments));
        emptyLayout.addView(emptyTextView1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 16, 0, 0));

        emptyTextView2 = new TextView(context);
        emptyTextView2.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyTextView2.setGravity(Gravity.CENTER);
        emptyTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        emptyTextView2.setPadding(AndroidUtilities.dp(20), 0, AndroidUtilities.dp(20), 0);
        emptyTextView2.setText(LocaleController.getString("PassportNoDocumentsInfo", R.string.PassportNoDocumentsInfo));
        emptyLayout.addView(emptyTextView2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 14, 0, 0));

        emptyTextView3 = new TextView(context);
        emptyTextView3.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        emptyTextView3.setGravity(Gravity.CENTER);
        emptyTextView3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        emptyTextView3.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        emptyTextView3.setGravity(Gravity.CENTER);
        emptyTextView3.setText(LocaleController.getString("PassportNoDocumentsAdd", R.string.PassportNoDocumentsAdd).toUpperCase());
        emptyLayout.addView(emptyTextView3, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 30, Gravity.CENTER, 0, 16, 0, 0));
        emptyTextView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddDocumentAlert();
            }
        });

        for (int a = 0, size = currentForm.values.size(); a < size; a++) {
            TLRPC.TL_secureValue value = currentForm.values.get(a);
            TLRPC.SecureValueType type;
            ArrayList<TLRPC.SecureValueType> documentTypes;
            if (value.type instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                    value.type instanceof TLRPC.TL_secureValueTypePassport ||
                    value.type instanceof TLRPC.TL_secureValueTypeInternalPassport ||
                    value.type instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                type = new TLRPC.TL_secureValueTypePersonalDetails();
                documentTypes = new ArrayList<>();
                documentTypes.add(value.type);
            } else if (value.type instanceof TLRPC.TL_secureValueTypeUtilityBill ||
                    value.type instanceof TLRPC.TL_secureValueTypeBankStatement ||
                    value.type instanceof TLRPC.TL_secureValueTypePassportRegistration ||
                    value.type instanceof TLRPC.TL_secureValueTypeTemporaryRegistration ||
                    value.type instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                type = new TLRPC.TL_secureValueTypeAddress();
                documentTypes = new ArrayList<>();
                documentTypes.add(value.type);
            } else {
                type = value.type;
                documentTypes = null;
            }
            addField(context, type, documentTypes, a == size - 1);
        }

        updateManageVisibility();
    }

    private boolean hasNotValueForType(Class<? extends TLRPC.SecureValueType> type) {
        for (int a = 0, count = currentForm.values.size(); a < count; a++) {
            if (currentForm.values.get(a).type.getClass() == type) {
                return false;
            }
        }
        return true;
    }

    private boolean hasUnfilledValues() {
        return hasNotValueForType(TLRPC.TL_secureValueTypePhone.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeEmail.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypePersonalDetails.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypePassport.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeInternalPassport.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeIdentityCard.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeDriverLicense.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeAddress.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeUtilityBill.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypePassportRegistration.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeTemporaryRegistration.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeBankStatement.class) ||
                hasNotValueForType(TLRPC.TL_secureValueTypeRentalAgreement.class);
    }

    private void openAddDocumentAlert() {
        ArrayList<CharSequence> values = new ArrayList<>();
        final ArrayList<Class<? extends TLRPC.SecureValueType>> types = new ArrayList<>();

        if (hasNotValueForType(TLRPC.TL_secureValueTypePhone.class)) {
            values.add(LocaleController.getString("ActionBotDocumentPhone", R.string.ActionBotDocumentPhone));
            types.add(TLRPC.TL_secureValueTypePhone.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeEmail.class)) {
            values.add(LocaleController.getString("ActionBotDocumentEmail", R.string.ActionBotDocumentEmail));
            types.add(TLRPC.TL_secureValueTypeEmail.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypePersonalDetails.class)) {
            values.add(LocaleController.getString("ActionBotDocumentIdentity", R.string.ActionBotDocumentIdentity));
            types.add(TLRPC.TL_secureValueTypePersonalDetails.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypePassport.class)) {
            values.add(LocaleController.getString("ActionBotDocumentPassport", R.string.ActionBotDocumentPassport));
            types.add(TLRPC.TL_secureValueTypePassport.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeInternalPassport.class)) {
            values.add(LocaleController.getString("ActionBotDocumentInternalPassport", R.string.ActionBotDocumentInternalPassport));
            types.add(TLRPC.TL_secureValueTypeInternalPassport.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypePassportRegistration.class)) {
            values.add(LocaleController.getString("ActionBotDocumentPassportRegistration", R.string.ActionBotDocumentPassportRegistration));
            types.add(TLRPC.TL_secureValueTypePassportRegistration.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeTemporaryRegistration.class)) {
            values.add(LocaleController.getString("ActionBotDocumentTemporaryRegistration", R.string.ActionBotDocumentTemporaryRegistration));
            types.add(TLRPC.TL_secureValueTypeTemporaryRegistration.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeIdentityCard.class)) {
            values.add(LocaleController.getString("ActionBotDocumentIdentityCard", R.string.ActionBotDocumentIdentityCard));
            types.add(TLRPC.TL_secureValueTypeIdentityCard.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeDriverLicense.class)) {
            values.add(LocaleController.getString("ActionBotDocumentDriverLicence", R.string.ActionBotDocumentDriverLicence));
            types.add(TLRPC.TL_secureValueTypeDriverLicense.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeAddress.class)) {
            values.add(LocaleController.getString("ActionBotDocumentAddress", R.string.ActionBotDocumentAddress));
            types.add(TLRPC.TL_secureValueTypeAddress.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeUtilityBill.class)) {
            values.add(LocaleController.getString("ActionBotDocumentUtilityBill", R.string.ActionBotDocumentUtilityBill));
            types.add(TLRPC.TL_secureValueTypeUtilityBill.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeBankStatement.class)) {
            values.add(LocaleController.getString("ActionBotDocumentBankStatement", R.string.ActionBotDocumentBankStatement));
            types.add(TLRPC.TL_secureValueTypeBankStatement.class);
        }
        if (hasNotValueForType(TLRPC.TL_secureValueTypeRentalAgreement.class)) {
            values.add(LocaleController.getString("ActionBotDocumentRentalAgreement", R.string.ActionBotDocumentRentalAgreement));
            types.add(TLRPC.TL_secureValueTypeRentalAgreement.class);
        }

        if (getParentActivity() == null || values.isEmpty()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("PassportNoDocumentsAdd", R.string.PassportNoDocumentsAdd));
        builder.setItems(values.toArray(new CharSequence[values.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TLRPC.SecureValueType type = null;
                TLRPC.SecureValueType documentType = null;
                try {
                    type = types.get(which).newInstance();
                } catch (Exception ignore) {

                }

                if (type instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                        type instanceof TLRPC.TL_secureValueTypePassport ||
                        type instanceof TLRPC.TL_secureValueTypeInternalPassport ||
                        type instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                    documentType = type;
                    type = new TLRPC.TL_secureValueTypePersonalDetails();
                } else if (type instanceof TLRPC.TL_secureValueTypeUtilityBill ||
                        type instanceof TLRPC.TL_secureValueTypeBankStatement ||
                        type instanceof TLRPC.TL_secureValueTypePassportRegistration ||
                        type instanceof TLRPC.TL_secureValueTypeTemporaryRegistration ||
                        type instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                    documentType = type;
                    type = new TLRPC.TL_secureValueTypeAddress();
                }

                openTypeActivity(type, documentType, new ArrayList<TLRPC.SecureValueType>());
            }
        });
        showDialog(builder.create());
    }

    private void updateManageVisibility() {
        if (currentForm.values.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            sectionCell.setVisibility(View.GONE);
            headerCell.setVisibility(View.GONE);
            addDocumentCell.setVisibility(View.GONE);
            deletePassportCell.setVisibility(View.GONE);
            addDocumentSectionCell.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            sectionCell.setVisibility(View.VISIBLE);
            headerCell.setVisibility(View.VISIBLE);
            deletePassportCell.setVisibility(View.VISIBLE);
            addDocumentSectionCell.setVisibility(View.VISIBLE);

            if (hasUnfilledValues()) {
                addDocumentCell.setVisibility(View.VISIBLE);
            } else {
                addDocumentCell.setVisibility(View.GONE);
            }
        }
    }

    private void callCallback(boolean success) {
        if (!callbackCalled) {
            if (!TextUtils.isEmpty(currentCallbackUrl)) {
                if (success) {
                    Browser.openUrl(getParentActivity(), Uri.parse(currentCallbackUrl + "&tg_passport=success"));
                } else if (!ignoreOnFailure && (currentActivityType == TYPE_PASSWORD || currentActivityType == TYPE_REQUEST)) {
                    Browser.openUrl(getParentActivity(), Uri.parse(currentCallbackUrl + "&tg_passport=cancel"));
                }
                callbackCalled = true;
            } else if (needActivityResult) {
                if (success || (!ignoreOnFailure && (currentActivityType == TYPE_PASSWORD || currentActivityType == TYPE_REQUEST))) {
                    getParentActivity().setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
                }
                callbackCalled = true;
            }
        }
    }

    private void createEmailInterface(Context context) {
        actionBar.setTitle(LocaleController.getString("PassportEmail", R.string.PassportEmail));

        if (!TextUtils.isEmpty(currentEmail)) {
            TextSettingsCell settingsCell1 = new TextSettingsCell(context);
            settingsCell1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
            settingsCell1.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            settingsCell1.setText(LocaleController.formatString("PassportPhoneUseSame", R.string.PassportPhoneUseSame, currentEmail), false);
            linearLayout2.addView(settingsCell1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            settingsCell1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    useCurrentValue = true;
                    doneItem.callOnClick();
                    useCurrentValue = false;
                }
            });

            bottomCell = new TextInfoPrivacyCell(context);
            bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            bottomCell.setText(LocaleController.getString("PassportPhoneUseSameEmailInfo", R.string.PassportPhoneUseSameEmailInfo));
            linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        inputFields = new EditTextBoldCursor[1];
        for (int a = 0; a < 1; a++) {
            ViewGroup container = new FrameLayout(context);
            linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            inputFields[a] = new EditTextBoldCursor(context);
            inputFields[a].setTag(a);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            inputFields[a].setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            switch (a) {
                case FIELD_EMAIL:
                    inputFields[a].setHint(LocaleController.getString("PaymentShippingEmailPlaceholder", R.string.PaymentShippingEmailPlaceholder));
                    if (currentTypeValue != null && currentTypeValue.plain_data instanceof TLRPC.TL_securePlainEmail) {
                        TLRPC.TL_securePlainEmail securePlainEmail = (TLRPC.TL_securePlainEmail) currentTypeValue.plain_data;
                        if (!TextUtils.isEmpty(securePlainEmail.email)) {
                            inputFields[a].setText(securePlainEmail.email);
                        }
                    }
                    break;
            }
            inputFields[a].setSelection(inputFields[a].length());
            inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
            inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 12, 17, 6));

            inputFields[a].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT) {
                        doneItem.callOnClick();
                        return true;
                    }
                    return false;
                }
            });
        }

        bottomCell = new TextInfoPrivacyCell(context);
        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        bottomCell.setText(LocaleController.getString("PassportEmailUploadInfo", R.string.PassportEmailUploadInfo));
        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void createPhoneInterface(Context context) {
        actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));

        languageMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                countriesArray.add(0, args[2]);
                countriesMap.put(args[2], args[0]);
                codesMap.put(args[0], args[2]);
                if (args.length > 3) {
                    phoneFormatMap.put(args[0], args[3]);
                }
                languageMap.put(args[1], args[2]);
            }
            reader.close();
        } catch (Exception e) {
            FileLog.e(e);
        }

        Collections.sort(countriesArray, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        String currentPhone = UserConfig.getInstance(currentAccount).getCurrentUser().phone;
        TextSettingsCell settingsCell1 = new TextSettingsCell(context);
        settingsCell1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        settingsCell1.setBackgroundDrawable(Theme.getSelectorDrawable(true));
        settingsCell1.setText(LocaleController.formatString("PassportPhoneUseSame", R.string.PassportPhoneUseSame, PhoneFormat.getInstance().format("+" + currentPhone)), false);
        linearLayout2.addView(settingsCell1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        settingsCell1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useCurrentValue = true;
                doneItem.callOnClick();
                useCurrentValue = false;
            }
        });

        bottomCell = new TextInfoPrivacyCell(context);
        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        bottomCell.setText(LocaleController.getString("PassportPhoneUseSameInfo", R.string.PassportPhoneUseSameInfo));
        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString("PassportPhoneUseOther", R.string.PassportPhoneUseOther));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        inputFields = new EditTextBoldCursor[3];
        for (int a = 0; a < 3; a++) {

            if (a == FIELD_PHONE) {
                inputFields[a] = new HintEditText(context);
            } else {
                inputFields[a] = new EditTextBoldCursor(context);
            }

            ViewGroup container;
            if (a == FIELD_PHONECODE) {
                container = new LinearLayout(context);
                ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else if (a == FIELD_PHONE) {
                container = (ViewGroup) inputFields[FIELD_PHONECODE].getParent();
            } else {
                container = new FrameLayout(context);
                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }

            inputFields[a].setTag(a);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            if (a == FIELD_PHONECOUNTRY) {
                inputFields[a].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (getParentActivity() == null) {
                            return false;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            CountrySelectActivity fragment = new CountrySelectActivity(false);
                            fragment.setCountrySelectActivityDelegate(new CountrySelectActivity.CountrySelectActivityDelegate() {
                                @Override
                                public void didSelectCountry(String name, String shortName) {
                                    inputFields[FIELD_PHONECOUNTRY].setText(name);
                                    int index = countriesArray.indexOf(name);
                                    if (index != -1) {
                                        ignoreOnTextChange = true;
                                        String code = countriesMap.get(name);
                                        inputFields[FIELD_PHONECODE].setText(code);
                                        String hint = phoneFormatMap.get(code);
                                        inputFields[FIELD_PHONE].setHintText(hint != null ? hint.replace('X', '–') : null);
                                        ignoreOnTextChange = false;
                                    }
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AndroidUtilities.showKeyboard(inputFields[FIELD_PHONE]);
                                        }
                                    }, 300);
                                    inputFields[FIELD_PHONE].requestFocus();
                                    inputFields[FIELD_PHONE].setSelection(inputFields[FIELD_PHONE].length());
                                }
                            });
                            presentFragment(fragment);
                        }
                        return true;
                    }
                });
                inputFields[a].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
                inputFields[a].setInputType(0);
                inputFields[a].setFocusable(false);
            } else {
                inputFields[a].setInputType(InputType.TYPE_CLASS_PHONE);
                if (a == FIELD_PHONE) {
                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                } else {
                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                }
            }
            inputFields[a].setSelection(inputFields[a].length());

            if (a == FIELD_PHONECODE) {
                plusTextView = new TextView(context);
                plusTextView.setText("+");
                plusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                plusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                container.addView(plusTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 17, 12, 0, 6));

                inputFields[a].setPadding(AndroidUtilities.dp(10), 0, 0, 0);
                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = new InputFilter.LengthFilter(5);
                inputFields[a].setFilters(inputFilters);
                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                container.addView(inputFields[a], LayoutHelper.createLinear(55, LayoutHelper.WRAP_CONTENT, 0, 12, 16, 6));
                inputFields[a].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (ignoreOnTextChange) {
                            return;
                        }
                        ignoreOnTextChange = true;
                        String text = PhoneFormat.stripExceptNumbers(inputFields[FIELD_PHONECODE].getText().toString());
                        inputFields[FIELD_PHONECODE].setText(text);
                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
                        if (text.length() == 0) {
                            phoneField.setHintText(null);
                            phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
                            inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
                        } else {
                            String country;
                            boolean ok = false;
                            String textToSet = null;
                            if (text.length() > 4) {
                                for (int a = 4; a >= 1; a--) {
                                    String sub = text.substring(0, a);
                                    country = codesMap.get(sub);
                                    if (country != null) {
                                        ok = true;
                                        textToSet = text.substring(a, text.length()) + inputFields[FIELD_PHONE].getText().toString();
                                        inputFields[FIELD_PHONECODE].setText(text = sub);
                                        break;
                                    }
                                }
                                if (!ok) {
                                    textToSet = text.substring(1, text.length()) + inputFields[FIELD_PHONE].getText().toString();
                                    inputFields[FIELD_PHONECODE].setText(text = text.substring(0, 1));
                                }
                            }
                            country = codesMap.get(text);
                            boolean set = false;
                            if (country != null) {
                                int index = countriesArray.indexOf(country);
                                if (index != -1) {
                                    inputFields[FIELD_PHONECOUNTRY].setText(countriesArray.get(index));
                                    String hint = phoneFormatMap.get(text);
                                    set = true;
                                    if (hint != null) {
                                        phoneField.setHintText(hint.replace('X', '–'));
                                        phoneField.setHint(null);
                                    }
                                }
                            }
                            if (!set) {
                                phoneField.setHintText(null);
                                phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
                                inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
                            }
                            if (!ok) {
                                inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].getText().length());
                            }
                            if (textToSet != null) {
                                phoneField.requestFocus();
                                phoneField.setText(textToSet);
                                phoneField.setSelection(phoneField.length());
                            }
                        }
                        ignoreOnTextChange = false;
                    }
                });
            } else if (a == FIELD_PHONE) {
                inputFields[a].setPadding(0, 0, 0, 0);
                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                inputFields[a].setHintText(null);
                inputFields[a].setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
                container.addView(inputFields[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 17, 6));
                inputFields[a].addTextChangedListener(new TextWatcher() {
                    private int characterAction = -1;
                    private int actionPosition;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        if (count == 0 && after == 1) {
                            characterAction = 1;
                        } else if (count == 1 && after == 0) {
                            if (s.charAt(start) == ' ' && start > 0) {
                                characterAction = 3;
                                actionPosition = start - 1;
                            } else {
                                characterAction = 2;
                            }
                        } else {
                            characterAction = -1;
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (ignoreOnPhoneChange) {
                            return;
                        }
                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
                        int start = phoneField.getSelectionStart();
                        String phoneChars = "0123456789";
                        String str = phoneField.getText().toString();
                        if (characterAction == 3) {
                            str = str.substring(0, actionPosition) + str.substring(actionPosition + 1, str.length());
                            start--;
                        }
                        StringBuilder builder = new StringBuilder(str.length());
                        for (int a = 0; a < str.length(); a++) {
                            String ch = str.substring(a, a + 1);
                            if (phoneChars.contains(ch)) {
                                builder.append(ch);
                            }
                        }
                        ignoreOnPhoneChange = true;
                        String hint = phoneField.getHintText();
                        if (hint != null) {
                            for (int a = 0; a < builder.length(); a++) {
                                if (a < hint.length()) {
                                    if (hint.charAt(a) == ' ') {
                                        builder.insert(a, ' ');
                                        a++;
                                        if (start == a && characterAction != 2 && characterAction != 3) {
                                            start++;
                                        }
                                    }
                                } else {
                                    builder.insert(a, ' ');
                                    if (start == a + 1 && characterAction != 2 && characterAction != 3) {
                                        start++;
                                    }
                                    break;
                                }
                            }
                        }
                        phoneField.setText(builder);
                        if (start >= 0) {
                            phoneField.setSelection(start <= phoneField.length() ? start : phoneField.length());
                        }
                        phoneField.onTextChange();
                        ignoreOnPhoneChange = false;
                    }
                });
            } else {
                inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
                inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 12, 17, 6));
            }

            inputFields[a].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT) {
                        inputFields[FIELD_PHONE].requestFocus();
                        return true;
                    } else if (i == EditorInfo.IME_ACTION_DONE) {
                        doneItem.callOnClick();
                        return true;
                    }
                    return false;
                }
            });

            if (a == FIELD_PHONECOUNTRY) {
                View divider = new View(context);
                dividers.add(divider);
                divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
                container.addView(divider, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, Gravity.LEFT | Gravity.BOTTOM));
            }
        }


        String country = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                country = telephonyManager.getSimCountryIso().toUpperCase();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (country != null) {
            String countryName = languageMap.get(country);
            if (countryName != null) {
                int index = countriesArray.indexOf(countryName);
                if (index != -1) {
                    inputFields[FIELD_PHONECODE].setText(countriesMap.get(countryName));
                }
            }
        }

        bottomCell = new TextInfoPrivacyCell(context);
        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        bottomCell.setText(LocaleController.getString("PassportPhoneUploadInfo", R.string.PassportPhoneUploadInfo));
        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void createAddressInterface(Context context) {
        languageMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                languageMap.put(args[1], args[2]);
            }
            reader.close();
        } catch (Exception e) {
            FileLog.e(e);
        }

        if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentRentalAgreement", R.string.ActionBotDocumentRentalAgreement));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeBankStatement) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentBankStatement", R.string.ActionBotDocumentBankStatement));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeUtilityBill) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentUtilityBill", R.string.ActionBotDocumentUtilityBill));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypePassportRegistration) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentPassportRegistration", R.string.ActionBotDocumentPassportRegistration));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeTemporaryRegistration) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentTemporaryRegistration", R.string.ActionBotDocumentTemporaryRegistration));
        } else {
            actionBar.setTitle(LocaleController.getString("PassportAddress", R.string.PassportAddress));
        }

        if (currentDocumentsType != null) {
            headerCell = new HeaderCell(context);
            headerCell.setText(LocaleController.getString("PassportDocuments", R.string.PassportDocuments));
            headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            documentsLayout = new LinearLayout(context);
            documentsLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout2.addView(documentsLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            uploadDocumentCell = new TextSettingsCell(context);
            uploadDocumentCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            linearLayout2.addView(uploadDocumentCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            uploadDocumentCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadingFileType = UPLOADING_TYPE_DOCUMENTS;
                    openAttachMenu();
                }
            });

            bottomCell = new TextInfoPrivacyCell(context);
            bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));

            if (currentBotId != 0) {
                noAllDocumentsErrorText = LocaleController.getString("PassportAddAddressUploadInfo", R.string.PassportAddAddressUploadInfo);
            } else {
                if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                    noAllDocumentsErrorText = LocaleController.getString("PassportAddAgreementInfo", R.string.PassportAddAgreementInfo);
                } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeUtilityBill) {
                    noAllDocumentsErrorText = LocaleController.getString("PassportAddBillInfo", R.string.PassportAddBillInfo);
                } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypePassportRegistration) {
                    noAllDocumentsErrorText = LocaleController.getString("PassportAddPassportRegistrationInfo", R.string.PassportAddPassportRegistrationInfo);
                } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeTemporaryRegistration) {
                    noAllDocumentsErrorText = LocaleController.getString("PassportAddTemporaryRegistrationInfo", R.string.PassportAddTemporaryRegistrationInfo);
                } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeBankStatement) {
                    noAllDocumentsErrorText = LocaleController.getString("PassportAddBankInfo", R.string.PassportAddBankInfo);
                } else {
                    noAllDocumentsErrorText = "";
                }
            }

            CharSequence text = noAllDocumentsErrorText;
            if (documentsErrors != null) {
                String errorText;
                if ((errorText = documentsErrors.get("files_all")) != null) {
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder(errorText);
                    stringBuilder.append("\n\n");
                    stringBuilder.append(noAllDocumentsErrorText);
                    text = stringBuilder;
                    stringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_windowBackgroundWhiteRedText3)), 0, errorText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    errorsValues.put("files_all", "");
                }
            }
            bottomCell.setText(text);
            linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString("PassportAddressHeader", R.string.PassportAddressHeader));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        inputFields = new EditTextBoldCursor[FIELD_ADDRESS_COUNT];
        for (int a = 0; a < FIELD_ADDRESS_COUNT; a++) {
            final EditTextBoldCursor field = new EditTextBoldCursor(context);
            inputFields[a] = field;

            ViewGroup container = new FrameLayout(context) {

                private StaticLayout errorLayout;
                float offsetX;

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    int width = MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(34);
                    errorLayout = field.getErrorLayout(width);
                    if (errorLayout != null) {
                        int lineCount = errorLayout.getLineCount();
                        if (lineCount > 1) {
                            int height = AndroidUtilities.dp(64) + (errorLayout.getLineBottom(lineCount - 1) - errorLayout.getLineBottom(0));
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                        }
                        if (LocaleController.isRTL) {
                            float maxW = 0;
                            for (int a = 0; a < lineCount; a++) {
                                float l = errorLayout.getLineLeft(a);
                                if (l != 0) {
                                    offsetX = 0;
                                    break;
                                }
                                maxW = Math.max(maxW, errorLayout.getLineWidth(a));
                                if (a == lineCount - 1) {
                                    offsetX = width - maxW;
                                }
                            }
                        }
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

                @Override
                protected void onDraw(Canvas canvas) {
                    if (errorLayout != null) {
                        canvas.save();
                        canvas.translate(AndroidUtilities.dp(17) + offsetX, field.getLineY() + AndroidUtilities.dp(3));
                        errorLayout.draw(canvas);
                        canvas.restore();
                    }
                }
            };
            container.setWillNotDraw(false);
            linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            if (a == FIELD_ADDRESS_COUNT - 1) {
                extraBackgroundView = new View(context);
                extraBackgroundView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                linearLayout2.addView(extraBackgroundView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6));
            }

            if (currentBotId == 0 && currentDocumentsType != null) {
                container.setVisibility(View.GONE);
                if (extraBackgroundView != null) {
                    extraBackgroundView.setVisibility(View.GONE);
                }
            }

            inputFields[a].setTag(a);
            inputFields[a].setSupportRtlHint(true);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setHeaderHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            inputFields[a].setTransformHintToHeader(true);
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            inputFields[a].setLineColors(Theme.getColor(Theme.key_windowBackgroundWhiteInputField), Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated), Theme.getColor(Theme.key_windowBackgroundWhiteRedText3));
            if (a == FIELD_COUNTRY) {
                inputFields[a].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (getParentActivity() == null) {
                            return false;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            CountrySelectActivity fragment = new CountrySelectActivity(false);
                            fragment.setCountrySelectActivityDelegate(new CountrySelectActivity.CountrySelectActivityDelegate() {
                                @Override
                                public void didSelectCountry(String name, String shortName) {
                                    inputFields[FIELD_COUNTRY].setText(name);
                                    currentCitizeship = shortName;
                                }
                            });
                            presentFragment(fragment);
                        }
                        return true;
                    }
                });
                inputFields[a].setInputType(0);
                inputFields[a].setFocusable(false);
            } else {
                inputFields[a].setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                inputFields[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            }
            String value;
            final String key;
            switch (a) {
                case FIELD_STREET1:
                    inputFields[a].setHintText(LocaleController.getString("PassportStreet1", R.string.PassportStreet1));
                    key = "street_line1";
                    break;
                case FIELD_STREET2:
                    inputFields[a].setHintText(LocaleController.getString("PassportStreet2", R.string.PassportStreet2));
                    key = "street_line2";
                    break;
                case FIELD_CITY:
                    inputFields[a].setHintText(LocaleController.getString("PassportCity", R.string.PassportCity));
                    key = "city";
                    break;
                case FIELD_STATE:
                    inputFields[a].setHintText(LocaleController.getString("PassportState", R.string.PassportState));
                    key = "state";
                    break;
                case FIELD_COUNTRY:
                    inputFields[a].setHintText(LocaleController.getString("PassportCountry", R.string.PassportCountry));
                    key = "country_code";
                    break;
                case FIELD_POSTCODE:
                    inputFields[a].setHintText(LocaleController.getString("PassportPostcode", R.string.PassportPostcode));
                    key = "post_code";
                    break;
                default:
                    continue;
            }
            setFieldValues(inputFields[a], key);
            if (a == FIELD_POSTCODE) {
                inputFields[a].addTextChangedListener(new TextWatcher() {

                    private boolean ignore;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (ignore) {
                            return;
                        }
                        ignore = true;
                        boolean error = false;
                        for (int a = 0; a < s.length(); a++) {
                            char ch = s.charAt(a);
                            if (!(ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '-' || ch == ' ')) {
                                error = true;
                                break;
                            }
                        }
                        ignore = false;
                        if (error) {
                            field.setErrorText(LocaleController.getString("PassportUseLatinOnly", R.string.PassportUseLatinOnly));
                        } else {
                            checkFieldForError(field, key, s);
                        }
                    }
                });
                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = new InputFilter.LengthFilter(10);
                inputFields[a].setFilters(inputFilters);
            } else {
                inputFields[a].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkFieldForError(field, key, s);
                    }
                });
            }

            inputFields[a].setSelection(inputFields[a].length());
            inputFields[a].setPadding(0, 0, 0, 0);
            inputFields[a].setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 64, Gravity.LEFT | Gravity.TOP, 17, 0, 17, 0));

            inputFields[a].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT) {
                        int num = (Integer) textView.getTag();
                        num++;
                        if (num < inputFields.length) {
                            if (inputFields[num].isFocusable()) {
                                inputFields[num].requestFocus();
                            } else {
                                inputFields[num].dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                                textView.clearFocus();
                                AndroidUtilities.hideKeyboard(textView);
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        sectionCell = new ShadowSectionCell(context);
        linearLayout2.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if (currentBotId == 0 && currentDocumentsType != null) {
            headerCell.setVisibility(View.GONE);
            sectionCell.setVisibility(View.GONE);
        }

        if ((currentBotId != 0 || currentDocumentsType == null) && currentTypeValue != null || currentDocumentsTypeValue != null) {
            if (currentDocumentsTypeValue != null) {
                addDocumentViews(currentDocumentsTypeValue.files);
            }
            sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));

            TextSettingsCell settingsCell1 = new TextSettingsCell(context);
            settingsCell1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText3));
            settingsCell1.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            if (currentBotId == 0 && currentDocumentsType == null) {
                settingsCell1.setText(LocaleController.getString("PassportDeleteInfo", R.string.PassportDeleteInfo), false);
            } else {
                settingsCell1.setText(LocaleController.getString("PassportDeleteDocument", R.string.PassportDeleteDocument), false);
            }
            linearLayout2.addView(settingsCell1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            settingsCell1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDocumentDeleteAlert();
                }
            });

            sectionCell = new ShadowSectionCell(context);
            sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            linearLayout2.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else {
            sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            if (currentBotId == 0 && currentDocumentsType != null) {
                bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            }
        }
        updateUploadText(UPLOADING_TYPE_DOCUMENTS);
    }

    private void createDocumentDeleteAlert() {
        final boolean checks[] = new boolean[]{true};

        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentValues.clear();
                delegate.deleteValue(currentType, currentDocumentsType, checks[0], null, null);
                finishFragment();
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
        if (currentBotId == 0 && currentDocumentsType == null && currentType instanceof TLRPC.TL_secureValueTypeAddress) {
            builder.setMessage(LocaleController.getString("PassportDeleteAddressAlert", R.string.PassportDeleteAddressAlert));
        } else if (currentBotId == 0 && currentDocumentsType == null && currentType instanceof TLRPC.TL_secureValueTypePersonalDetails) {
            builder.setMessage(LocaleController.getString("PassportDeletePersonalAlert", R.string.PassportDeletePersonalAlert));
        } else {
            builder.setMessage(LocaleController.getString("PassportDeleteDocumentAlert", R.string.PassportDeleteDocumentAlert));
        }

        if (currentBotId != 0 && currentDocumentsType != null) {
            FrameLayout frameLayout = new FrameLayout(getParentActivity());
            CheckBoxCell cell = new CheckBoxCell(getParentActivity(), 1);
            cell.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            if (currentType instanceof TLRPC.TL_secureValueTypeAddress) {
                cell.setText(LocaleController.getString("PassportDeleteDocumentAddress", R.string.PassportDeleteDocumentAddress), "", true, false);
            } else if (currentType instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                cell.setText(LocaleController.getString("PassportDeleteDocumentPersonal", R.string.PassportDeleteDocumentPersonal), "", true, false);
            }
            cell.setPadding(LocaleController.isRTL ? AndroidUtilities.dp(16) : AndroidUtilities.dp(8), 0, LocaleController.isRTL ? AndroidUtilities.dp(8) : AndroidUtilities.dp(16), 0);
            frameLayout.addView(cell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.TOP | Gravity.LEFT));
            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isEnabled()) {
                        return;
                    }
                    CheckBoxCell cell = (CheckBoxCell) v;
                    checks[0] = !checks[0];
                    cell.setChecked(checks[0], true);
                }
            });
            builder.setView(frameLayout);
        }

        showDialog(builder.create());
    }

    private void onFieldError(View field) {
        if (field == null) {
            return;
        }
        Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(200);
        }
        AndroidUtilities.shakeView(field, 2, 0);
        while (field != null && linearLayout2.indexOfChild(field) < 0) {
            field = (View) field.getParent();
        }
        if (field != null) {
            scrollView.smoothScrollTo(0, field.getTop() - (scrollView.getMeasuredHeight() - field.getMeasuredHeight()) / 2);
        }
    }

    private String getDocumentHash(SecureDocument document) {
        if (document != null) {
            if (document.secureFile != null && document.secureFile.file_hash != null) {
                return Base64.encodeToString(document.secureFile.file_hash, Base64.NO_WRAP);
            } else if (document.fileHash != null) {
                return Base64.encodeToString(document.fileHash, Base64.NO_WRAP);
            }
        }
        return "";
    }

    private void checkFieldForError(EditTextBoldCursor field, String key, Editable s) {
        String value;
        if (errorsValues != null && (value = errorsValues.get(key)) != null) {
            if (TextUtils.equals(value, s)) {
                if (fieldsErrors != null && (value = fieldsErrors.get(key)) != null) {
                    field.setErrorText(value);
                } else if (documentsErrors != null && (value = documentsErrors.get(key)) != null) {
                    field.setErrorText(value);
                }
            } else {
                field.setErrorText(null);
            }
        } else {
            field.setErrorText(null);
        }
    }

    private boolean checkFieldsForError() {
        if (currentDocumentsType != null) {
            if (uploadDocumentCell != null) {
                if (documents.isEmpty()) {
                    onFieldError(uploadDocumentCell);
                    return true;
                } else {
                    for (int a = 0, size = documents.size(); a < size; a++) {
                        SecureDocument document = documents.get(a);
                        String key = "files" + getDocumentHash(document);
                        if (key != null && errorsValues.containsKey(key)) {
                            onFieldError(documentsCells.get(document));
                            return true;
                        }
                    }
                }
            }
            if (errorsValues.containsKey("files_all")) {
                onFieldError(bottomCell);
                return true;
            }
            if (uploadFrontCell != null) {
                if (frontDocument == null) {
                    onFieldError(uploadFrontCell);
                    return true;
                } else {
                    String key = "front" + getDocumentHash(frontDocument);
                    if (errorsValues.containsKey(key)) {
                        onFieldError(documentsCells.get(frontDocument));
                        return true;
                    }
                }
            }
            if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeIdentityCard || currentDocumentsType instanceof TLRPC.TL_secureValueTypeDriverLicense) {
                if (uploadReverseCell != null) {
                    if (reverseDocument == null) {
                        onFieldError(uploadReverseCell);
                        return true;
                    } else {
                        String key = "reverse" + getDocumentHash(reverseDocument);
                        if (errorsValues.containsKey(key)) {
                            onFieldError(documentsCells.get(reverseDocument));
                            return true;
                        }
                    }
                }
            }
            if (uploadSelfieCell != null) {
                if (selfieDocument == null) {
                    onFieldError(uploadSelfieCell);
                    return true;
                } else {
                    String key = "selfie" + getDocumentHash(selfieDocument);
                    if (errorsValues.containsKey(key)) {
                        onFieldError(documentsCells.get(selfieDocument));
                        return true;
                    }
                }
            }
        }
        for (int a = 0; a < inputFields.length; a++) {
            boolean error = false;
            if (inputFields[a].hasErrorText()) {
                error = true;
            }
            if (!errorsValues.isEmpty()) {
                String key;
                if (currentType instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                    switch (a) {
                        case FIELD_NAME:
                            key = "first_name";
                            break;
                        case FIELD_SURNAME:
                            key = "last_name";
                            break;
                        case FIELD_BIRTHDAY:
                            key = "birth_date";
                            break;
                        case FIELD_GENDER:
                            key = "gender";
                            break;
                        case FIELD_CITIZENSHIP:
                            key = "country_code";
                            break;
                        case FIELD_RESIDENCE:
                            key = "residence_country_code";
                            break;
                        case FIELD_CARDNUMBER:
                            key = "document_no";
                            break;
                        case FIELD_EXPIRE:
                            key = "expiry_date";
                            break;
                        default:
                            key = null;
                            break;
                    }
                } else if (currentType instanceof TLRPC.TL_secureValueTypeAddress) {
                    switch (a) {
                        case FIELD_STREET1:
                            key = "street_line1";
                            break;
                        case FIELD_STREET2:
                            key = "street_line2";
                            break;
                        case FIELD_CITY:
                            key = "city";
                            break;
                        case FIELD_STATE:
                            key = "state";
                            break;
                        case FIELD_COUNTRY:
                            key = "country_code";
                            break;
                        case FIELD_POSTCODE:
                            key = "post_code";
                            break;
                        default:
                            key = null;
                            break;
                    }
                } else {
                    key = null;
                }
                if (key != null) {
                    String value = errorsValues.get(key);
                    if (!TextUtils.isEmpty(value)) {
                        if (value.equals(inputFields[a].getText().toString())) {
                            error = true;
                        }
                    }
                }
            }
            if (currentBotId == 0) {
                if (currentDocumentsType != null && a < FIELD_CARDNUMBER) {
                    continue;
                }
            }
            if (!error) {
                int len = inputFields[a].length();
                if (currentActivityType == TYPE_IDENTITY) {
                    if (a == FIELD_EXPIRE) {
                        continue;
                    } else if (a == FIELD_NAME || a == FIELD_SURNAME) {
                        if (len > 255) {
                            error = true;
                        }
                    } else if (a == FIELD_CARDNUMBER) {
                        if (len > 24) {
                            error = true;
                        }
                    }
                } else if (currentActivityType == TYPE_ADDRESS) {
                    if (a == FIELD_STREET2) {
                        continue;
                    } else if (a == FIELD_CITY) {
                        if (len < 2) {
                            error = true;
                        }
                    } else if (a == FIELD_STATE) {
                        if ("US".equals(currentCitizeship)) {
                            if (len < 2) {
                                error = true;
                            }
                        } else {
                            continue;
                        }
                    } else if (a == FIELD_POSTCODE) {
                        if (len < 2 || len > 10) {
                            error = true;
                        }
                    }
                }
                if (!error && len == 0) {
                    error = true;
                }
            }
            if (error) {
                onFieldError(inputFields[a]);
                return true;
            }
        }
        return false;
    }

    private void createIdentityInterface(final Context context) {
        languageMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                languageMap.put(args[1], args[2]);
            }
            reader.close();
        } catch (Exception e) {
            FileLog.e(e);
        }

        if (currentDocumentsType != null) {
            headerCell = new HeaderCell(context);
            if (currentBotId == 0) {
                headerCell.setText(LocaleController.getString("PassportDocuments", R.string.PassportDocuments));
            } else {
                headerCell.setText(LocaleController.getString("PassportRequiredDocuments", R.string.PassportRequiredDocuments));
            }
            headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            frontLayout = new LinearLayout(context);
            frontLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout2.addView(frontLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            uploadFrontCell = new TextDetailSettingsCell(context);
            uploadFrontCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            linearLayout2.addView(uploadFrontCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            uploadFrontCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadingFileType = UPLOADING_TYPE_FRONT;
                    openAttachMenu();
                }
            });

            reverseLayout = new LinearLayout(context);
            reverseLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout2.addView(reverseLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            uploadReverseCell = new TextDetailSettingsCell(context);
            uploadReverseCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            uploadReverseCell.setTextAndValue(LocaleController.getString("PassportReverseSide", R.string.PassportReverseSide), LocaleController.getString("PassportReverseSideInfo", R.string.PassportReverseSideInfo), currentForm.selfie_required);
            linearLayout2.addView(uploadReverseCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            uploadReverseCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadingFileType = UPLOADING_TYPE_REVERSE;
                    openAttachMenu();
                }
            });

            if (currentForm.selfie_required) {
                selfieLayout = new LinearLayout(context);
                selfieLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout2.addView(selfieLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

                uploadSelfieCell = new TextDetailSettingsCell(context);
                uploadSelfieCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
                uploadSelfieCell.setTextAndValue(LocaleController.getString("PassportSelfie", R.string.PassportSelfie), LocaleController.getString("PassportSelfieInfo", R.string.PassportSelfieInfo), false);
                linearLayout2.addView(uploadSelfieCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                uploadSelfieCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadingFileType = UPLOADING_TYPE_SELFIE;
                        openAttachMenu();
                    }
                });
            }

            bottomCell = new TextInfoPrivacyCell(context);
            bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            bottomCell.setText(LocaleController.getString("PassportPersonalUploadInfo", R.string.PassportPersonalUploadInfo));
            linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else if (Build.VERSION.SDK_INT >= 18) {
            scanDocumentCell = new TextSettingsCell(context);
            scanDocumentCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            scanDocumentCell.setText(LocaleController.getString("PassportScanPassport", R.string.PassportScanPassport), false);
            linearLayout2.addView(scanDocumentCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            scanDocumentCell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 22);
                        return;
                    }
                    MrzCameraActivity fragment = new MrzCameraActivity();
                    fragment.setDelegate(new MrzCameraActivity.MrzCameraActivityDelegate() {
                        @Override
                        public void didFindMrzInfo(MrzRecognizer.Result result) {
                            if (!TextUtils.isEmpty(result.firstName)) {
                                inputFields[FIELD_NAME].setText(result.firstName);
                            }
                            if (!TextUtils.isEmpty(result.lastName)) {
                                inputFields[FIELD_SURNAME].setText(result.lastName);
                            }
                            if (result.gender != MrzRecognizer.Result.GENDER_UNKNOWN) {
                                switch (result.gender) {
                                    case MrzRecognizer.Result.GENDER_MALE:
                                        currentGender = "male";
                                        inputFields[FIELD_GENDER].setText(LocaleController.getString("PassportMale", R.string.PassportMale));
                                        break;
                                    case MrzRecognizer.Result.GENDER_FEMALE:
                                        currentGender = "female";
                                        inputFields[FIELD_GENDER].setText(LocaleController.getString("PassportFemale", R.string.PassportFemale));
                                        break;
                                }
                            }
                            if (!TextUtils.isEmpty(result.nationality)) {
                                currentCitizeship = result.nationality;
                                String country = languageMap.get(currentCitizeship);
                                if (country != null) {
                                    inputFields[FIELD_CITIZENSHIP].setText(country);
                                }
                            }
                            if (!TextUtils.isEmpty(result.issuingCountry)) {
                                currentResidence = result.issuingCountry;
                                String country = languageMap.get(currentResidence);
                                if (country != null) {
                                    inputFields[FIELD_RESIDENCE].setText(country);
                                }
                            }
                            if (result.birthDay > 0 && result.birthMonth > 0 && result.birthYear > 0) {
                                inputFields[FIELD_BIRTHDAY].setText(String.format(Locale.US, "%02d.%02d.%d", result.birthDay, result.birthMonth, result.birthYear));
                            }
                        }
                    });
                    presentFragment(fragment);
                }
            });

            bottomCell = new TextInfoPrivacyCell(context);
            bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            bottomCell.setText(LocaleController.getString("PassportScanPassportInfo", R.string.PassportScanPassportInfo));
            linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        headerCell = new HeaderCell(context);
        headerCell.setText(LocaleController.getString("PassportPersonal", R.string.PassportPersonal));
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        int count = currentDocumentsType != null ? FIELD_IDENTITY_COUNT : FIELD_IDENTITY_NODOC_COUNT;
        inputFields = new EditTextBoldCursor[count];

        for (int a = 0; a < count; a++) {
            final EditTextBoldCursor field = new EditTextBoldCursor(context);
            inputFields[a] = field;

            ViewGroup container = new FrameLayout(context) {

                private StaticLayout errorLayout;
                private float offsetX;

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    int width = MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(34);
                    errorLayout = field.getErrorLayout(width);
                    if (errorLayout != null) {
                        int lineCount = errorLayout.getLineCount();
                        if (lineCount > 1) {
                            int height = AndroidUtilities.dp(64) + (errorLayout.getLineBottom(lineCount - 1) - errorLayout.getLineBottom(0));
                            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                        }
                        if (LocaleController.isRTL) {
                            float maxW = 0;
                            for (int a = 0; a < lineCount; a++) {
                                float l = errorLayout.getLineLeft(a);
                                if (l != 0) {
                                    offsetX = 0;
                                    break;
                                }
                                maxW = Math.max(maxW, errorLayout.getLineWidth(a));
                                if (a == lineCount - 1) {
                                    offsetX = width - maxW;
                                }
                            }
                        }
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

                @Override
                protected void onDraw(Canvas canvas) {
                    if (errorLayout != null) {
                        canvas.save();
                        canvas.translate(AndroidUtilities.dp(17) + offsetX, field.getLineY() + AndroidUtilities.dp(3));
                        errorLayout.draw(canvas);
                        canvas.restore();
                    }
                }
            };
            container.setWillNotDraw(false);
            linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 64));
            container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            if (a == count - 1) {
                extraBackgroundView = new View(context);
                extraBackgroundView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                linearLayout2.addView(extraBackgroundView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 6));
            }

            if (currentBotId == 0 && currentDocumentsType != null && a < FIELD_CARDNUMBER) {
                container.setVisibility(View.GONE);
                if (extraBackgroundView != null) {
                    extraBackgroundView.setVisibility(View.GONE);
                }
            }

            inputFields[a].setTag(a);
            inputFields[a].setSupportRtlHint(true);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setHeaderHintColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
            inputFields[a].setTransformHintToHeader(true);
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            inputFields[a].setLineColors(Theme.getColor(Theme.key_windowBackgroundWhiteInputField), Theme.getColor(Theme.key_windowBackgroundWhiteInputFieldActivated), Theme.getColor(Theme.key_windowBackgroundWhiteRedText3));
            if (a == FIELD_CITIZENSHIP || a == FIELD_RESIDENCE) {
                inputFields[a].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View v, MotionEvent event) {
                        if (getParentActivity() == null) {
                            return false;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            CountrySelectActivity fragment = new CountrySelectActivity(false);
                            fragment.setCountrySelectActivityDelegate(new CountrySelectActivity.CountrySelectActivityDelegate() {
                                @Override
                                public void didSelectCountry(String name, String shortName) {
                                    int field = (Integer) v.getTag();
                                    final EditTextBoldCursor editText = inputFields[field];
                                    editText.setText(name);
                                    if (field == FIELD_CITIZENSHIP) {
                                        currentCitizeship = shortName;
                                    } else {
                                        currentResidence = shortName;
                                    }
                                }
                            });
                            presentFragment(fragment);
                        }
                        return true;
                    }
                });
                inputFields[a].setInputType(0);
            } else if (a == FIELD_BIRTHDAY || a == FIELD_EXPIRE) {
                inputFields[a].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View v, MotionEvent event) {
                        if (getParentActivity() == null) {
                            return false;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Calendar calendar = Calendar.getInstance();
                            int year = calendar.get(Calendar.YEAR);
                            int monthOfYear = calendar.get(Calendar.MONTH);
                            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                            try {
                                final EditTextBoldCursor field = (EditTextBoldCursor) v;
                                int num = (Integer) field.getTag();
                                int minYear;
                                int maxYear;
                                String title;
                                if (num == FIELD_EXPIRE) {
                                    title = LocaleController.getString("PassportSelectExpiredDate", R.string.PassportSelectExpiredDate);
                                    minYear = 0;
                                    maxYear = 20;
                                } else {
                                    title = LocaleController.getString("PassportSelectBithdayDate", R.string.PassportSelectBithdayDate);
                                    minYear = -120;
                                    maxYear = 0;
                                }
                                AlertDialog.Builder builder = AlertsCreator.createDatePickerDialog(context, minYear, maxYear, title, num == FIELD_EXPIRE, new AlertsCreator.DatePickerDelegate() {
                                    @Override
                                    public void didSelectDate(int year, int month, int dayOfMonth) {
                                        currentExpireDate[0] = year;
                                        currentExpireDate[1] = month + 1;
                                        currentExpireDate[2] = dayOfMonth;
                                        field.setText(String.format(Locale.US, "%02d.%02d.%d", dayOfMonth, month + 1, year));
                                    }
                                });
                                if (num == FIELD_EXPIRE) {
                                    builder.setNegativeButton(LocaleController.getString("PassportSelectNotExpire", R.string.PassportSelectNotExpire), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            currentExpireDate[0] = currentExpireDate[1] = currentExpireDate[2] = 0;
                                            field.setText(LocaleController.getString("PassportNoExpireDate", R.string.PassportNoExpireDate));
                                        }
                                    });
                                }
                                showDialog(builder.create());
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                        return true;
                    }
                });
                inputFields[a].setInputType(0);
                inputFields[a].setFocusable(false);
            } else if (a == FIELD_GENDER) {
                inputFields[a].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View v, MotionEvent event) {
                        if (getParentActivity() == null) {
                            return false;
                        }
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setTitle(LocaleController.getString("PassportSelectGender", R.string.PassportSelectGender));
                            builder.setItems(new CharSequence[]{
                                    LocaleController.getString("PassportMale", R.string.PassportMale),
                                    LocaleController.getString("PassportFemale", R.string.PassportFemale)
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == 0) {
                                        currentGender = "male";
                                        inputFields[FIELD_GENDER].setText(LocaleController.getString("PassportMale", R.string.PassportMale));
                                    } else if (i == 1) {
                                        currentGender = "female";
                                        inputFields[FIELD_GENDER].setText(LocaleController.getString("PassportFemale", R.string.PassportFemale));
                                    }
                                }
                            });
                            builder.setPositiveButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                        return true;
                    }
                });
                inputFields[a].setInputType(0);
                inputFields[a].setFocusable(false);
            } else {
                inputFields[a].setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                inputFields[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            }
            String value;
            final String key;
            switch (a) {
                case FIELD_NAME:
                    inputFields[a].setHintText(LocaleController.getString("PassportName", R.string.PassportName));
                    key = "first_name";
                    break;
                case FIELD_SURNAME:
                    inputFields[a].setHintText(LocaleController.getString("PassportSurname", R.string.PassportSurname));
                    key = "last_name";
                    break;
                case FIELD_BIRTHDAY:
                    inputFields[a].setHintText(LocaleController.getString("PassportBirthdate", R.string.PassportBirthdate));
                    key = "birth_date";
                    break;
                case FIELD_GENDER:
                    inputFields[a].setHintText(LocaleController.getString("PassportGender", R.string.PassportGender));
                    key = "gender";
                    break;
                case FIELD_CITIZENSHIP:
                    inputFields[a].setHintText(LocaleController.getString("PassportCitizenship", R.string.PassportCitizenship));
                    key = "country_code";
                    break;
                case FIELD_RESIDENCE:
                    inputFields[a].setHintText(LocaleController.getString("PassportResidence", R.string.PassportResidence));
                    key = "residence_country_code";
                    break;
                case FIELD_CARDNUMBER:
                    inputFields[a].setHintText(LocaleController.getString("PassportDocumentNumber", R.string.PassportDocumentNumber));
                    key = "document_no";
                    break;
                case FIELD_EXPIRE:
                    inputFields[a].setHintText(LocaleController.getString("PassportExpired", R.string.PassportExpired));
                    key = "expiry_date";
                    break;
                default:
                    continue;
            }
            setFieldValues(inputFields[a], key);
            inputFields[a].setSelection(inputFields[a].length());
            if (a == FIELD_NAME || a == FIELD_SURNAME) {
                inputFields[a].addTextChangedListener(new TextWatcher() {

                    private boolean ignore;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (ignore) {
                            return;
                        }
                        ignore = true;
                        boolean error = false;
                        for (int a = 0; a < s.length(); a++) {
                            char ch = s.charAt(a);
                            if (!(ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == ' ' || ch == '\'' || ch == ',' || ch == '.' || ch == '&' || ch == '-' || ch == '/')) {
                                error = true;
                                break;
                            }
                        }
                        ignore = false;
                        if (error) {
                            field.setErrorText(LocaleController.getString("PassportUseLatinOnly", R.string.PassportUseLatinOnly));
                        } else {
                            checkFieldForError(field, key, s);
                        }
                    }
                });
            } else {
                inputFields[a].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkFieldForError(field, key, s);
                    }
                });
            }

            inputFields[a].setPadding(0, 0, 0, 0);
            inputFields[a].setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 17, 0, 17, 0));

            inputFields[a].setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT) {
                        int num = (Integer) textView.getTag();
                        num++;
                        if (num < inputFields.length) {
                            if (inputFields[num].isFocusable()) {
                                inputFields[num].requestFocus();
                            } else {
                                inputFields[num].dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0));
                                textView.clearFocus();
                                AndroidUtilities.hideKeyboard(textView);
                            }
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        sectionCell = new ShadowSectionCell(context);
        linearLayout2.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        if ((currentBotId != 0 || currentDocumentsType == null) && currentTypeValue != null || currentDocumentsTypeValue != null) {
            if (currentDocumentsTypeValue != null) {
                addDocumentViews(currentDocumentsTypeValue.files);
                if (currentDocumentsTypeValue.front_side instanceof TLRPC.TL_secureFile) {
                    addDocumentViewInternal((TLRPC.TL_secureFile) currentDocumentsTypeValue.front_side, UPLOADING_TYPE_FRONT);
                }
                if (currentDocumentsTypeValue.reverse_side instanceof TLRPC.TL_secureFile) {
                    addDocumentViewInternal((TLRPC.TL_secureFile) currentDocumentsTypeValue.reverse_side, UPLOADING_TYPE_REVERSE);
                }
                if (currentDocumentsTypeValue.selfie instanceof TLRPC.TL_secureFile) {
                    addDocumentViewInternal((TLRPC.TL_secureFile) currentDocumentsTypeValue.selfie, UPLOADING_TYPE_SELFIE);
                }
            }
            sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));

            TextSettingsCell settingsCell1 = new TextSettingsCell(context);
            settingsCell1.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText3));
            settingsCell1.setBackgroundDrawable(Theme.getSelectorDrawable(true));
            if (currentBotId == 0 && currentDocumentsType == null) {
                settingsCell1.setText(LocaleController.getString("PassportDeleteInfo", R.string.PassportDeleteInfo), false);
            } else {
                settingsCell1.setText(LocaleController.getString("PassportDeleteDocument", R.string.PassportDeleteDocument), false);
            }
            linearLayout2.addView(settingsCell1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            settingsCell1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createDocumentDeleteAlert();
                }
            });

            sectionCell = new ShadowSectionCell(context);
            sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            linearLayout2.addView(sectionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else {
            sectionCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        }
        updateInterfaceStringsForDocumentType();
    }

    private void updateInterfaceStringsForDocumentType() {
        if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeIdentityCard) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentIdentityCard", R.string.ActionBotDocumentIdentityCard));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypePassport) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentPassport", R.string.ActionBotDocumentPassport));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeInternalPassport) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentInternalPassport", R.string.ActionBotDocumentInternalPassport));
        } else if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeDriverLicense) {
            actionBar.setTitle(LocaleController.getString("ActionBotDocumentDriverLicence", R.string.ActionBotDocumentDriverLicence));
        } else {
            actionBar.setTitle(LocaleController.getString("PassportPersonal", R.string.PassportPersonal));
        }
        updateUploadText(UPLOADING_TYPE_FRONT);
        updateUploadText(UPLOADING_TYPE_REVERSE);
        updateUploadText(UPLOADING_TYPE_SELFIE);
    }

    private void updateUploadText(int type) {
        if (type == UPLOADING_TYPE_DOCUMENTS) {
            if (uploadDocumentCell == null) {
                return;
            }
            if (documents.size() >= 1) {
                uploadDocumentCell.setText(LocaleController.getString("PassportUploadAdditinalDocument", R.string.PassportUploadAdditinalDocument), false);
            } else {
                uploadDocumentCell.setText(LocaleController.getString("PassportUploadDocument", R.string.PassportUploadDocument), false);
            }
        } else if (type == UPLOADING_TYPE_SELFIE) {
            if (uploadSelfieCell == null) {
                return;
            }
            uploadSelfieCell.setVisibility(selfieDocument != null ? View.GONE : View.VISIBLE);
        } else if (type == UPLOADING_TYPE_FRONT) {
            if (uploadFrontCell == null) {
                return;
            }
            boolean divider = currentForm.selfie_required || currentDocumentsType instanceof TLRPC.TL_secureValueTypeIdentityCard || currentDocumentsType instanceof TLRPC.TL_secureValueTypeDriverLicense;
            if (currentDocumentsType instanceof TLRPC.TL_secureValueTypePassport || currentDocumentsType instanceof TLRPC.TL_secureValueTypeInternalPassport) {
                uploadFrontCell.setTextAndValue(LocaleController.getString("PassportMainPage", R.string.PassportMainPage), LocaleController.getString("PassportMainPageInfo", R.string.PassportMainPageInfo), divider);
            } else {
                uploadFrontCell.setTextAndValue(LocaleController.getString("PassportFrontSide", R.string.PassportFrontSide), LocaleController.getString("PassportFrontSideInfo", R.string.PassportFrontSideInfo), divider);
            }
            uploadFrontCell.setVisibility(frontDocument != null ? View.GONE : View.VISIBLE);
        } else if (type == UPLOADING_TYPE_REVERSE) {
            if (uploadReverseCell == null) {
                return;
            }
            if (currentDocumentsType instanceof TLRPC.TL_secureValueTypeIdentityCard || currentDocumentsType instanceof TLRPC.TL_secureValueTypeDriverLicense) {
                reverseLayout.setVisibility(View.VISIBLE);
                uploadReverseCell.setVisibility(reverseDocument != null ? View.GONE : View.VISIBLE);
            } else {
                reverseLayout.setVisibility(View.GONE);
                uploadReverseCell.setVisibility(View.GONE);
            }
        }
    }

    private void addDocumentViewInternal(TLRPC.TL_secureFile f, int uploadingType) {
        SecureDocumentKey secureDocumentKey = getSecureDocumentKey(f.secret, f.file_hash);
        SecureDocument secureDocument = new SecureDocument(secureDocumentKey, f, null, null, null);
        addDocumentView(secureDocument, uploadingType);
    }

    private void addDocumentViews(ArrayList<TLRPC.SecureFile> files) {
        documents.clear();
        for (int a = 0, size = files.size(); a < size; a++) {
            TLRPC.SecureFile secureFile = files.get(a);
            if (secureFile instanceof TLRPC.TL_secureFile) {
                addDocumentViewInternal((TLRPC.TL_secureFile) secureFile, UPLOADING_TYPE_DOCUMENTS);
            }
        }
    }

    private void setFieldValues(EditTextBoldCursor editText, String key) {
        String value;
        if ((value = currentValues.get(key)) != null) {
            switch (key) {
                case "country_code": {
                    currentCitizeship = value;
                    String country = languageMap.get(currentCitizeship);
                    if (country != null) {
                        editText.setText(country);
                    }
                    break;
                }
                case "residence_country_code": {
                    currentResidence = value;
                    String country = languageMap.get(currentResidence);
                    if (country != null) {
                        editText.setText(country);
                    }
                    break;
                }
                case "gender":
                    if ("male".equals(value)) {
                        currentGender = value;
                        editText.setText(LocaleController.getString("PassportMale", R.string.PassportMale));
                    } else if ("female".equals(value)) {
                        currentGender = value;
                        editText.setText(LocaleController.getString("PassportFemale", R.string.PassportFemale));
                    }
                    break;
                case "expiry_date":
                    boolean ok = false;
                    if (!TextUtils.isEmpty(value)) {
                        String args[] = value.split("\\.");
                        if (args.length == 3) {
                            currentExpireDate[0] = Utilities.parseInt(args[2]);
                            currentExpireDate[1] = Utilities.parseInt(args[1]);
                            currentExpireDate[2] = Utilities.parseInt(args[0]);
                            editText.setText(value);
                            ok = true;
                        }
                    }
                    if (!ok) {
                        currentExpireDate[0] = currentExpireDate[1] = currentExpireDate[2] = 0;
                        editText.setText(LocaleController.getString("PassportNoExpireDate", R.string.PassportNoExpireDate));
                    }
                    break;
                default:
                    editText.setText(value);
                    break;
            }
        }
        if (fieldsErrors != null && (value = fieldsErrors.get(key)) != null) {
            editText.setErrorText(value);
            errorsValues.put(key, editText.getText().toString());
        } else if (documentsErrors != null && (value = documentsErrors.get(key)) != null) {
            editText.setErrorText(value);
            errorsValues.put(key, editText.getText().toString());
        }
    }

    private void addDocumentView(final SecureDocument document, final int type) {
        if (type == UPLOADING_TYPE_SELFIE) {
            selfieDocument = document;
            if (selfieLayout == null) {
                return;
            }
        } else if (type == UPLOADING_TYPE_FRONT) {
            frontDocument = document;
            if (frontLayout == null) {
                return;
            }
        } else if (type == UPLOADING_TYPE_REVERSE) {
            reverseDocument = document;
            if (reverseLayout == null) {
                return;
            }
        } else {
            documents.add(document);
            if (documentsLayout == null) {
                return;
            }
        }
        if (getParentActivity() == null) {
            return;
        }
        final SecureDocumentCell cell = new SecureDocumentCell(getParentActivity());

        String value;
        final String key;

        cell.setTag(document);
        cell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
        String text;
        documentsCells.put(document, cell);
        String hash = getDocumentHash(document);
        if (type == UPLOADING_TYPE_SELFIE) {
            text = LocaleController.getString("PassportSelfie", R.string.PassportSelfie);
            selfieLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            key = "selfie" + hash;
        } else if (type == UPLOADING_TYPE_FRONT) {
            if (currentDocumentsType instanceof TLRPC.TL_secureValueTypePassport || currentDocumentsType instanceof TLRPC.TL_secureValueTypeInternalPassport) {
                text = LocaleController.getString("PassportMainPage", R.string.PassportMainPage);
            } else {
                text = LocaleController.getString("PassportFrontSide", R.string.PassportFrontSide);
            }
            frontLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            key = "front" + hash;
        } else if (type == UPLOADING_TYPE_REVERSE) {
            text = LocaleController.getString("PassportReverseSide", R.string.PassportReverseSide);
            reverseLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            key = "reverse" + hash;
        } else {
            text = LocaleController.getString("AttachPhoto", R.string.AttachPhoto);
            documentsLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            key = "files" + hash;
        }

        if (key == null || documentsErrors == null || (value = documentsErrors.get(key)) == null) {
            value = LocaleController.formatDateForBan(document.secureFile.date);
        } else {
            cell.valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText3));
            errorsValues.put(key, "");
        }

        cell.setTextAndValueAndImage(text, value, document);
        cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadingFileType = type;
                if (type == UPLOADING_TYPE_SELFIE) {
                    currentPhotoViewerLayout = selfieLayout;
                } else if (type == UPLOADING_TYPE_FRONT) {
                    currentPhotoViewerLayout = frontLayout;
                } else if (type == UPLOADING_TYPE_REVERSE) {
                    currentPhotoViewerLayout = reverseLayout;
                } else {
                    currentPhotoViewerLayout = documentsLayout;
                }
                SecureDocument document = (SecureDocument) v.getTag();
                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                if (type == UPLOADING_TYPE_DOCUMENTS) {
                    PhotoViewer.getInstance().openPhoto(documents, documents.indexOf(document), provider);
                } else {
                    ArrayList<SecureDocument> arrayList = new ArrayList<>();
                    arrayList.add(document);
                    PhotoViewer.getInstance().openPhoto(arrayList, 0, provider);
                }
            }
        });
        cell.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                if (type == UPLOADING_TYPE_SELFIE) {
                    builder.setMessage(LocaleController.getString("PassportDeleteSelfie", R.string.PassportDeleteSelfie));
                } else {
                    builder.setMessage(LocaleController.getString("PassportDeleteScan", R.string.PassportDeleteScan));
                }
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        documentsCells.remove(document);
                        if (type == UPLOADING_TYPE_SELFIE) {
                            selfieDocument = null;
                            selfieLayout.removeView(cell);
                        } else if (type == UPLOADING_TYPE_FRONT) {
                            frontDocument = null;
                            frontLayout.removeView(cell);
                        } else if (type == UPLOADING_TYPE_REVERSE) {
                            reverseDocument = null;
                            reverseLayout.removeView(cell);
                        } else {
                            documents.remove(document);
                            documentsLayout.removeView(cell);
                        }

                        if (key != null) {
                            if (documentsErrors != null) {
                                documentsErrors.remove(key);
                            }
                            if (errorsValues != null) {
                                errorsValues.remove(key);
                            }
                        }

                        updateUploadText(type);
                        if (document.path != null && uploadingDocuments.remove(document.path) != null) {
                            if (uploadingDocuments.isEmpty()) {
                                doneItem.setEnabled(true);
                                doneItem.setAlpha(1.0f);
                            }
                            FileLoader.getInstance(currentAccount).cancelUploadFile(document.path, false);
                        }
                    }
                });
                showDialog(builder.create());
                return true;
            }
        });
    }

    private String getNameForType(TLRPC.SecureValueType type) {
        if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
            return "personal_details";
        } else if (type instanceof TLRPC.TL_secureValueTypePassport) {
            return "passport";
        } else if (type instanceof TLRPC.TL_secureValueTypeInternalPassport) {
            return "internal_passport";
        } else if (type instanceof TLRPC.TL_secureValueTypeDriverLicense) {
            return "driver_license";
        } else if (type instanceof TLRPC.TL_secureValueTypeIdentityCard) {
            return "identity_card";
        } else if (type instanceof TLRPC.TL_secureValueTypeUtilityBill) {
            return "utility_bill";
        } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
            return "address";
        } else if (type instanceof TLRPC.TL_secureValueTypeBankStatement) {
            return "bank_statement";
        } else if (type instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
            return "rental_agreement";
        } else if (type instanceof TLRPC.TL_secureValueTypeTemporaryRegistration) {
            return "temporary_registration";
        } else if (type instanceof TLRPC.TL_secureValueTypePassportRegistration) {
            return "passport_registration";
        } else if (type instanceof TLRPC.TL_secureValueTypeEmail) {
            return "email";
        } else if (type instanceof TLRPC.TL_secureValueTypePhone) {
            return "phone";
        }
        return "";
    }

    private void setTypeValue(TLRPC.SecureValueType type, String text, String json, TLRPC.SecureValueType documentType, String documentsJson) {
        TextDetailSecureCell view = typesViews.get(type);
        if (view == null) {
            if (currentActivityType == TYPE_MANAGE) {
                ArrayList<TLRPC.SecureValueType> documentTypes = new ArrayList<>();
                if (documentType != null) {
                    documentTypes.add(documentType);
                }
                View prev = linearLayout2.getChildAt(linearLayout2.getChildCount() - 6);
                if (prev instanceof TextDetailSecureCell) {
                    ((TextDetailSecureCell) prev).setNeedDivider(true);
                }
                view = addField(getParentActivity(), type, documentTypes, true);
                updateManageVisibility();
            } else {
                return;
            }
        }
        HashMap<String, String> values = typesValues.get(type);

        if (json != null) {
            languageMap = new HashMap<>();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(ApplicationLoader.applicationContext.getResources().getAssets().open("countries.txt")));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] args = line.split(";");
                    languageMap.put(args[1], args[2]);
                }
                reader.close();
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else {
            languageMap = null;
        }

        String value = null;
        if (json != null || documentsJson != null) {
            if (values == null) {
                return;
            }
            values.clear();
            String keys[] = null;
            String documentKeys[] = null;
            if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                if (currentActivityType == TYPE_REQUEST || currentActivityType == TYPE_MANAGE && documentType == null) {
                    keys = new String[]{
                            "first_name",
                            "last_name",
                            "birth_date",
                            "gender",
                            "country_code",
                            "residence_country_code"
                    };
                }
                if (currentActivityType == TYPE_REQUEST || currentActivityType == TYPE_MANAGE && documentType != null) {
                    documentKeys = new String[]{
                            "document_no",
                            "expiry_date"
                    };
                }
            } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
                if (currentActivityType == TYPE_REQUEST || currentActivityType == TYPE_MANAGE && documentType == null) {
                    keys = new String[]{
                            "street_line1",
                            "street_line2",
                            "post_code",
                            "city",
                            "state",
                            "country_code"
                    };
                }
            }
            if (keys != null || documentKeys != null) {
                try {
                    StringBuilder stringBuilder = null;
                    JSONObject jsonObject = null;
                    String currentKeys[] = null;
                    for (int b = 0; b < 2; b++) {
                        if (b == 0) {
                            if (json != null) {
                                jsonObject = new JSONObject(json);
                                currentKeys = keys;
                            }
                        } else {
                            if (documentsJson != null) {
                                jsonObject = new JSONObject(documentsJson);
                                currentKeys = documentKeys;
                            }
                        }
                        if (currentKeys == null) {
                            continue;
                        }
                        if (currentActivityType != TYPE_MANAGE && b == 0 && documentType != null && !TextUtils.isEmpty(documentsJson)) {
                            if (stringBuilder == null) {
                                stringBuilder = new StringBuilder();
                            }
                            if (documentType instanceof TLRPC.TL_secureValueTypePassport) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentPassport", R.string.ActionBotDocumentPassport));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeDriverLicense) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentDriverLicence", R.string.ActionBotDocumentDriverLicence));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentIdentityCard", R.string.ActionBotDocumentIdentityCard));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeUtilityBill) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentUtilityBill", R.string.ActionBotDocumentUtilityBill));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeBankStatement) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentBankStatement", R.string.ActionBotDocumentBankStatement));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentRentalAgreement", R.string.ActionBotDocumentRentalAgreement));
                            } else if (hasNotValueForType(TLRPC.TL_secureValueTypeInternalPassport.class)) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentInternalPassport", R.string.ActionBotDocumentInternalPassport));
                            } else if (hasNotValueForType(TLRPC.TL_secureValueTypePassportRegistration.class)) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentPassportRegistration", R.string.ActionBotDocumentPassportRegistration));
                            } else if (hasNotValueForType(TLRPC.TL_secureValueTypeTemporaryRegistration.class)) {
                                stringBuilder.append(LocaleController.getString("ActionBotDocumentTemporaryRegistration", R.string.ActionBotDocumentTemporaryRegistration));
                            }
                        }
                        for (int a = 0; a < currentKeys.length; a++) {
                            if (jsonObject.has(currentKeys[a])) {
                                if (stringBuilder == null) {
                                    stringBuilder = new StringBuilder();
                                }
                                String jsonValue = jsonObject.getString(currentKeys[a]);
                                if (jsonValue != null) {
                                    values.put(currentKeys[a], jsonValue);
                                    if (!TextUtils.isEmpty(jsonValue)) {
                                        if (stringBuilder.length() > 0) {
                                            if ("last_name".equals(currentKeys[a])) {
                                                stringBuilder.append(" ");
                                            } else {
                                                stringBuilder.append(", ");
                                            }
                                        }
                                        switch (currentKeys[a]) {
                                            case "country_code":
                                                String country = languageMap.get(jsonValue);
                                                if (country != null) {
                                                    stringBuilder.append(country);
                                                }
                                                break;
                                            case "gender":
                                                if ("male".equals(jsonValue)) {
                                                    stringBuilder.append(LocaleController.getString("PassportMale", R.string.PassportMale));
                                                } else if ("female".equals(jsonValue)) {
                                                    stringBuilder.append(LocaleController.getString("PassportFemale", R.string.PassportFemale));
                                                }
                                                break;
                                            default:
                                                stringBuilder.append(jsonValue);
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (stringBuilder != null) {
                        value = stringBuilder.toString();
                    }
                } catch (Exception ignore) {

                }
            }
        } else if (text != null) {
            if (type instanceof TLRPC.TL_secureValueTypePhone) {
                value = PhoneFormat.getInstance().format("+" + text);
            } else if (type instanceof TLRPC.TL_secureValueTypeEmail) {
                value = text;
            }
        }

        boolean isError = false;
        HashMap<String, String> errors = errorsMap.get(getNameForType(type));
        HashMap<String, String> documentsErrors = errorsMap.get(getNameForType(documentType));
        if (errors != null && errors.size() > 0 || documentsErrors != null && documentsErrors.size() > 0) {
            value = LocaleController.getString("PassportCorrectErrors", R.string.PassportCorrectErrors);
            //value = getErrorsString(errors, documentsErrors);
            isError = true;
        } else {
            if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                if (TextUtils.isEmpty(value)) {
                    if (documentType == null) {
                        value = LocaleController.getString("PassportPersonalDetailsInfo", R.string.PassportPersonalDetailsInfo);
                    } else {
                        if (currentActivityType == TYPE_MANAGE) {
                            value = LocaleController.getString("PassportDocuments", R.string.PassportDocuments);
                        } else {
                            value = LocaleController.getString("PassportIdentityDocumentInfo", R.string.PassportIdentityDocumentInfo);
                        }
                    }
                }
            } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
                if (TextUtils.isEmpty(value)) {
                    if (documentType == null) {
                        value = LocaleController.getString("PassportAddressNoUploadInfo", R.string.PassportAddressNoUploadInfo);
                    } else {
                        if (currentActivityType == TYPE_MANAGE) {
                            value = LocaleController.getString("PassportDocuments", R.string.PassportDocuments);
                        } else {
                            value = LocaleController.getString("PassportAddressInfo", R.string.PassportAddressInfo);
                        }
                    }
                }
            } else if (type instanceof TLRPC.TL_secureValueTypePhone) {
                if (TextUtils.isEmpty(value)) {
                    value = LocaleController.getString("PassportPhoneInfo", R.string.PassportPhoneInfo);
                }
            } else if (type instanceof TLRPC.TL_secureValueTypeEmail) {
                if (TextUtils.isEmpty(value)) {
                    value = LocaleController.getString("PassportEmailInfo", R.string.PassportEmailInfo);
                }
            }
        }
        view.setValue(value);
        view.valueTextView.setTextColor(Theme.getColor(isError ? Theme.key_windowBackgroundWhiteRedText3 : Theme.key_windowBackgroundWhiteGrayText2));
        view.setChecked(!isError && currentActivityType != TYPE_MANAGE && getValueByType(type, true) != null && (documentType == null || getValueByType(documentType, true) != null));
    }

    private String getErrorsString(HashMap<String, String> errors, HashMap<String, String> documentErrors) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int a = 0; a < 2; a++) {
            HashMap<String, String> hashMap;
            if (a == 0) {
                hashMap = errors;
            } else {
                hashMap = documentErrors;
            }
            if (hashMap == null) {
                continue;
            }
            for (HashMap.Entry<String, String> entry : hashMap.entrySet()) {
                String value = entry.getValue();
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                    value = value.toLowerCase();
                }
                if (value.endsWith(".")) {
                    value = value.substring(0, value.length() - 1);
                }
                stringBuilder.append(value);
            }
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.append('.');
        }
        return stringBuilder.toString();
    }

    private TLRPC.TL_secureValue getValueByType(TLRPC.SecureValueType type, boolean check) {
        if (type == null) {
            return null;
        }
        for (int a = 0, size = currentForm.values.size(); a < size; a++) {
            TLRPC.TL_secureValue secureValue = currentForm.values.get(a);
            if (type.getClass() == secureValue.type.getClass()) {
                if (check) {
                    if (currentForm.selfie_required &&
                            (type instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                                    type instanceof TLRPC.TL_secureValueTypePassport ||
                                    type instanceof TLRPC.TL_secureValueTypeInternalPassport ||
                                    type instanceof TLRPC.TL_secureValueTypeIdentityCard)) {
                        if (!(secureValue.selfie instanceof TLRPC.TL_secureFile)) {
                            return null;
                        }
                    }
                    if (type instanceof TLRPC.TL_secureValueTypeUtilityBill ||
                            type instanceof TLRPC.TL_secureValueTypeBankStatement ||
                            type instanceof TLRPC.TL_secureValueTypePassportRegistration ||
                            type instanceof TLRPC.TL_secureValueTypeTemporaryRegistration ||
                            type instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                        if (secureValue.files.isEmpty()) {
                            return null;
                        }
                    }
                    if (type instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                            type instanceof TLRPC.TL_secureValueTypePassport ||
                            type instanceof TLRPC.TL_secureValueTypeInternalPassport ||
                            type instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                        if (!(secureValue.front_side instanceof TLRPC.TL_secureFile)) {
                            return null;
                        }
                    }
                    if (type instanceof TLRPC.TL_secureValueTypeDriverLicense ||
                            type instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                        if (!(secureValue.reverse_side instanceof TLRPC.TL_secureFile)) {
                            return null;
                        }
                    }
                }

                return secureValue;
            }
        }
        return null;
    }

    private void openTypeActivity(TLRPC.SecureValueType type, TLRPC.SecureValueType documentsType, ArrayList<TLRPC.SecureValueType> availableDocumentTypes) {
        int activityType = -1;
        if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
            activityType = TYPE_IDENTITY;
        } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
            activityType = TYPE_ADDRESS;
        } else if (type instanceof TLRPC.TL_secureValueTypePhone) {
            activityType = TYPE_PHONE;
        } else if (type instanceof TLRPC.TL_secureValueTypeEmail) {
            activityType = TYPE_EMAIL;
        }
        if (activityType != -1) {
            HashMap<String, String> errors = errorsMap.get(getNameForType(type));
            HashMap<String, String> documentsErrors = errorsMap.get(getNameForType(documentsType));
            TLRPC.TL_secureValue value = getValueByType(type, false);
            TLRPC.TL_secureValue documentsValue = getValueByType(documentsType, false);

            final PassportActivity activity = new PassportActivity(activityType, currentForm, currentPassword, type, value, documentsType, documentsValue, typesValues.get(type));
            activity.delegate = new PassportActivityDelegate() {

                private TLRPC.InputSecureFile getInputSecureFile(SecureDocument document) {
                    if (document.inputFile != null) {
                        TLRPC.TL_inputSecureFileUploaded inputSecureFileUploaded = new TLRPC.TL_inputSecureFileUploaded();
                        inputSecureFileUploaded.id = document.inputFile.id;
                        inputSecureFileUploaded.parts = document.inputFile.parts;
                        inputSecureFileUploaded.md5_checksum = document.inputFile.md5_checksum;
                        inputSecureFileUploaded.file_hash = document.fileHash;
                        inputSecureFileUploaded.secret = document.fileSecret;
                        return inputSecureFileUploaded;
                    } else {
                        TLRPC.TL_inputSecureFile inputSecureFile = new TLRPC.TL_inputSecureFile();
                        inputSecureFile.id = document.secureFile.id;
                        inputSecureFile.access_hash = document.secureFile.access_hash;
                        return inputSecureFile;
                    }
                }

                private void renameFile(SecureDocument oldDocument, TLRPC.TL_secureFile newSecureFile) {
                    File oldFile = FileLoader.getPathToAttach(oldDocument);
                    String oldKey = oldDocument.secureFile.dc_id + "_" + oldDocument.secureFile.id;
                    File newFile = FileLoader.getPathToAttach(newSecureFile);
                    String newKey = newSecureFile.dc_id + "_" + newSecureFile.id;
                    oldFile.renameTo(newFile);
                    ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, null, false);
                }

                @Override
                public void saveValue(final TLRPC.SecureValueType type, final String text, final String json, final TLRPC.SecureValueType documentsType, final String documentsJson, final ArrayList<SecureDocument> documents, final SecureDocument selfie, final SecureDocument front, final SecureDocument reverse, final Runnable finishRunnable, final ErrorRunnable errorRunnable) {
                    TLRPC.TL_inputSecureValue inputSecureValue = null;

                    if (!TextUtils.isEmpty(json)) {
                        inputSecureValue = new TLRPC.TL_inputSecureValue();
                        inputSecureValue.type = type;
                        inputSecureValue.flags |= 1;

                        EncryptionResult result = encryptData(AndroidUtilities.getStringBytes(json));
                        inputSecureValue.data = new TLRPC.TL_secureData();
                        inputSecureValue.data.data = result.encryptedData;
                        inputSecureValue.data.data_hash = result.fileHash;
                        inputSecureValue.data.secret = result.fileSecret;
                    } else if (!TextUtils.isEmpty(text)) {
                        TLRPC.SecurePlainData plainData;
                        if (type instanceof TLRPC.TL_secureValueTypeEmail) {
                            TLRPC.TL_securePlainEmail securePlainEmail = new TLRPC.TL_securePlainEmail();
                            securePlainEmail.email = text;
                            plainData = securePlainEmail;
                        } else if (type instanceof TLRPC.TL_secureValueTypePhone) {
                            TLRPC.TL_securePlainPhone securePlainPhone = new TLRPC.TL_securePlainPhone();
                            securePlainPhone.phone = text;
                            plainData = securePlainPhone;
                        } else {
                            return;
                        }
                        inputSecureValue = new TLRPC.TL_inputSecureValue();
                        inputSecureValue.type = type;
                        inputSecureValue.flags |= 32;

                        inputSecureValue.plain_data = plainData;
                    }

                    if (inputSecureValue == null) {
                        if (errorRunnable != null) {
                            errorRunnable.onError(null, null);
                        }
                        return;
                    }

                    TLRPC.TL_inputSecureValue fileInputSecureValue;
                    if (documentsType != null) {
                        fileInputSecureValue = new TLRPC.TL_inputSecureValue();
                        fileInputSecureValue.type = documentsType;

                        if (!TextUtils.isEmpty(documentsJson)) {
                            fileInputSecureValue.flags |= 1;

                            EncryptionResult result = encryptData(AndroidUtilities.getStringBytes(documentsJson));
                            fileInputSecureValue.data = new TLRPC.TL_secureData();
                            fileInputSecureValue.data.data = result.encryptedData;
                            fileInputSecureValue.data.data_hash = result.fileHash;
                            fileInputSecureValue.data.secret = result.fileSecret;
                        }

                        if (front != null) {
                            fileInputSecureValue.front_side = getInputSecureFile(front);
                            fileInputSecureValue.flags |= 2;
                        }
                        if (reverse != null) {
                            fileInputSecureValue.reverse_side = getInputSecureFile(reverse);
                            fileInputSecureValue.flags |= 4;
                        }
                        if (selfie != null) {
                            fileInputSecureValue.selfie = getInputSecureFile(selfie);
                            fileInputSecureValue.flags |= 8;
                        }
                        if (documents != null && !documents.isEmpty()) {
                            fileInputSecureValue.flags |= 16;
                            for (int a = 0, size = documents.size(); a < size; a++) {
                                fileInputSecureValue.files.add(getInputSecureFile(documents.get(a)));
                            }
                        }

                        if (currentActivityType == TYPE_MANAGE) {
                            inputSecureValue = fileInputSecureValue;
                            fileInputSecureValue = null;
                        }
                    } else {
                        fileInputSecureValue = null;
                    }

                    final PassportActivityDelegate currentDelegate = this;
                    final TLRPC.TL_inputSecureValue finalFileInputSecureValue = fileInputSecureValue;

                    final TLRPC.TL_account_saveSecureValue req = new TLRPC.TL_account_saveSecureValue();
                    req.value = inputSecureValue;
                    req.secure_secret_id = secureSecretId;
                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {

                        private void onResult(final TLRPC.TL_error error, final TLRPC.TL_secureValue newValue, final TLRPC.TL_secureValue newPendingValue) {
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (error != null) {
                                        if (errorRunnable != null) {
                                            errorRunnable.onError(error.text, text);
                                        }
                                        AlertsCreator.processError(currentAccount, error, PassportActivity.this, req, text);
                                    } else {
                                        if (currentActivityType == TYPE_MANAGE) {
                                            if (documentsType != null) {
                                                removeValue(documentsType);
                                            } else {
                                                removeValue(type);
                                            }
                                        } else {
                                            removeValue(type);
                                            removeValue(documentsType);
                                        }
                                        if (newValue != null) {
                                            currentForm.values.add(newValue);
                                        }
                                        if (newPendingValue != null) {
                                            currentForm.values.add(newPendingValue);
                                        }
                                        if (documents != null && !documents.isEmpty()) {
                                            for (int a = 0, size = documents.size(); a < size; a++) {
                                                SecureDocument document = documents.get(a);
                                                if (document.inputFile != null) {
                                                    for (int b = 0, size2 = newValue.files.size(); b < size2; b++) {
                                                        TLRPC.SecureFile file = newValue.files.get(b);
                                                        if (file instanceof TLRPC.TL_secureFile) {
                                                            TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) file;
                                                            if (Utilities.arraysEquals(document.fileSecret, 0, secureFile.secret, 0)) {
                                                                renameFile(document, secureFile);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (selfie != null && selfie.inputFile != null && newValue.selfie instanceof TLRPC.TL_secureFile) {
                                            TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) newValue.selfie;
                                            if (Utilities.arraysEquals(selfie.fileSecret, 0, secureFile.secret, 0)) {
                                                renameFile(selfie, secureFile);
                                            }
                                        }
                                        if (front != null && front.inputFile != null && newValue.front_side instanceof TLRPC.TL_secureFile) {
                                            TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) newValue.front_side;
                                            if (Utilities.arraysEquals(front.fileSecret, 0, secureFile.secret, 0)) {
                                                renameFile(front, secureFile);
                                            }
                                        }
                                        if (reverse != null && reverse.inputFile != null && newValue.reverse_side instanceof TLRPC.TL_secureFile) {
                                            TLRPC.TL_secureFile secureFile = (TLRPC.TL_secureFile) newValue.reverse_side;
                                            if (Utilities.arraysEquals(reverse.fileSecret, 0, secureFile.secret, 0)) {
                                                renameFile(reverse, secureFile);
                                            }
                                        }

                                        setTypeValue(type, text, json, documentsType, documentsJson);
                                        if (finishRunnable != null) {
                                            finishRunnable.run();
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void run(final TLObject response, final TLRPC.TL_error error) {
                            if (error != null) {
                                if (error.text.equals("EMAIL_VERIFICATION_NEEDED")) {
                                    TLRPC.TL_account_sendVerifyEmailCode req = new TLRPC.TL_account_sendVerifyEmailCode();
                                    req.email = text;
                                    ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                                        @Override
                                        public void run(final TLObject response, final TLRPC.TL_error error) {
                                            AndroidUtilities.runOnUIThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (response != null) {
                                                        TLRPC.TL_account_sentEmailCode res = (TLRPC.TL_account_sentEmailCode) response;
                                                        HashMap<String, String> values = new HashMap<>();
                                                        values.put("email", text);
                                                        values.put("pattern", res.email_pattern);
                                                        PassportActivity activity = new PassportActivity(TYPE_EMAIL_VERIFICATION, currentForm, currentPassword, type, null, null, null, values);
                                                        activity.currentAccount = currentAccount;
                                                        activity.emailCodeLength = res.length;
                                                        activity.saltedPassword = saltedPassword;
                                                        activity.secureSecret = secureSecret;
                                                        activity.delegate = currentDelegate;
                                                        presentFragment(activity, true);
                                                    } else {
                                                        showAlertWithText(LocaleController.getString("PassportEmail", R.string.PassportEmail), error.text);
                                                        if (errorRunnable != null) {
                                                            errorRunnable.onError(error.text, text);
                                                        }
                                                    }
                                                }
                                            });

                                        }
                                    });
                                    return;
                                } else if (error.text.equals("PHONE_VERIFICATION_NEEDED")) {
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            errorRunnable.onError(error.text, text);
                                        }
                                    });
                                    return;
                                }
                            }
                            if (error == null && finalFileInputSecureValue != null) {
                                final TLRPC.TL_secureValue pendingValue = (TLRPC.TL_secureValue) response;
                                final TLRPC.TL_account_saveSecureValue req = new TLRPC.TL_account_saveSecureValue();
                                req.value = finalFileInputSecureValue;
                                req.secure_secret_id = secureSecretId;
                                ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                                    @Override
                                    public void run(final TLObject response, final TLRPC.TL_error error) {
                                        onResult(error, (TLRPC.TL_secureValue) response, pendingValue);
                                    }
                                });
                            } else {
                                onResult(error, (TLRPC.TL_secureValue) response, null);
                            }
                        }
                    });
                }

                @Override
                public SecureDocument saveFile(TLRPC.TL_secureFile secureFile) {
                    String path = FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE) + "/" + secureFile.dc_id + "_" + secureFile.id + ".jpg";
                    EncryptionResult result = createSecureDocument(path);
                    return new SecureDocument(result.secureDocumentKey, secureFile, path, result.fileHash, result.fileSecret);
                }

                @Override
                public void deleteValue(TLRPC.SecureValueType type, TLRPC.SecureValueType documentsType, boolean deleteType, Runnable finishRunnable, ErrorRunnable errorRunnable) {
                    deleteValueInternal(type, documentsType, deleteType, finishRunnable, errorRunnable);
                }
            };
            activity.currentAccount = currentAccount;
            activity.saltedPassword = saltedPassword;
            activity.secureSecret = secureSecret;
            activity.currentBotId = currentBotId;
            activity.fieldsErrors = errors;
            activity.documentsErrors = documentsErrors;
            activity.availableDocumentTypes = availableDocumentTypes;
            if (activityType == TYPE_EMAIL) {
                activity.currentEmail = currentEmail;
            }
            presentFragment(activity);
        }
    }

    private TLRPC.TL_secureValue removeValue(TLRPC.SecureValueType type) {
        if (type == null) {
            return null;
        }
        for (int a = 0, size = currentForm.values.size(); a < size; a++) {
            TLRPC.TL_secureValue secureValue = currentForm.values.get(a);
            if (type.getClass() == secureValue.type.getClass()) {
                return currentForm.values.remove(a);
            }
        }
        return null;
    }

    private void deleteValueInternal(final TLRPC.SecureValueType type, final TLRPC.SecureValueType documentsType, final boolean deleteType, final Runnable finishRunnable, final ErrorRunnable errorRunnable) {
        if (type == null) {
            return;
        }
        TLRPC.TL_account_deleteSecureValue req = new TLRPC.TL_account_deleteSecureValue();
        if (currentActivityType == TYPE_MANAGE && documentsType != null) {
            req.types.add(documentsType);
        } else {
            if (deleteType) {
                req.types.add(type);
            }
            if (documentsType != null) {
                req.types.add(documentsType);
            }
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error != null) {
                            if (errorRunnable != null) {
                                errorRunnable.onError(error.text, null);
                            }
                            showAlertWithText(LocaleController.getString("AppName", R.string.AppName), error.text);
                        } else {
                            if (currentActivityType == TYPE_MANAGE) {
                                if (documentsType != null) {
                                    removeValue(documentsType);
                                } else {
                                    removeValue(type);
                                }
                            } else {
                                if (deleteType) {
                                    removeValue(type);
                                }
                                removeValue(documentsType);
                            }
                            if (currentActivityType == TYPE_MANAGE) {
                                TextDetailSecureCell view = typesViews.remove(type);
                                if (view != null) {
                                    linearLayout2.removeView(view);
                                    View child = linearLayout2.getChildAt(linearLayout2.getChildCount() - 6);
                                    if (child instanceof TextDetailSecureCell) {
                                        ((TextDetailSecureCell) child).setNeedDivider(false);
                                    }
                                }
                                updateManageVisibility();
                            } else {
                                if (deleteType) {
                                    setTypeValue(type, null, null, documentsType, null);
                                } else {
                                    String json = null;
                                    TLRPC.TL_secureValue value = getValueByType(type, false);
                                    if (value != null && value.data != null) {
                                        json = decryptData(value.data.data, decryptValueSecret(value.data.secret, value.data.data_hash), value.data.data_hash);
                                    }
                                    setTypeValue(type, null, json, documentsType, null);
                                }
                            }
                            if (finishRunnable != null) {
                                finishRunnable.run();
                            }
                        }
                    }
                });
            }
        });
    }

    private TextDetailSecureCell addField(Context context, final TLRPC.SecureValueType type, final ArrayList<TLRPC.SecureValueType> documentTypes, boolean last) {
        TextDetailSecureCell view = new TextDetailSecureCell(context);
        view.setBackgroundDrawable(Theme.getSelectorDrawable(true));
        if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
            String text;
            if (documentTypes == null || documentTypes.isEmpty()) {
                text = LocaleController.getString("PassportPersonalDetails", R.string.PassportPersonalDetails);
            } else if (documentTypes.size() == 1) {
                TLRPC.SecureValueType documentType = documentTypes.get(0);
                if (documentType instanceof TLRPC.TL_secureValueTypePassport) {
                    text = LocaleController.getString("ActionBotDocumentPassport", R.string.ActionBotDocumentPassport);
                } else if (documentType instanceof TLRPC.TL_secureValueTypeDriverLicense) {
                    text = LocaleController.getString("ActionBotDocumentDriverLicence", R.string.ActionBotDocumentDriverLicence);
                } else if (documentType instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                    text = LocaleController.getString("ActionBotDocumentIdentityCard", R.string.ActionBotDocumentIdentityCard);
                } else if (documentType instanceof TLRPC.TL_secureValueTypeInternalPassport) {
                    text = LocaleController.getString("ActionBotDocumentInternalPassport", R.string.ActionBotDocumentInternalPassport);
                } else {
                    text = "LOC_ERR: NO NAME FOR ID TYPE";
                }
            } else {
                text = LocaleController.getString("PassportIdentityDocument", R.string.PassportIdentityDocument);
            }
            view.setTextAndValue(text, "", !last);
        } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
            String text;
            if (documentTypes == null || documentTypes.isEmpty()) {
                text = LocaleController.getString("PassportAddress", R.string.PassportAddress);
            } else if (documentTypes.size() == 1) {
                TLRPC.SecureValueType documentType = documentTypes.get(0);
                if (documentType instanceof TLRPC.TL_secureValueTypeUtilityBill) {
                    text = LocaleController.getString("ActionBotDocumentUtilityBill", R.string.ActionBotDocumentUtilityBill);
                } else if (documentType instanceof TLRPC.TL_secureValueTypeBankStatement) {
                    text = LocaleController.getString("ActionBotDocumentBankStatement", R.string.ActionBotDocumentBankStatement);
                } else if (documentType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                    text = LocaleController.getString("ActionBotDocumentRentalAgreement", R.string.ActionBotDocumentRentalAgreement);
                } else if (documentType instanceof TLRPC.TL_secureValueTypePassportRegistration) {
                    text = LocaleController.getString("ActionBotDocumentPassportRegistration", R.string.ActionBotDocumentPassportRegistration);
                } else if (documentType instanceof TLRPC.TL_secureValueTypeTemporaryRegistration) {
                    text = LocaleController.getString("ActionBotDocumentTemporaryRegistration", R.string.ActionBotDocumentTemporaryRegistration);
                } else {
                    text = "LOC_ERR: NO NAME FOR ADDRESS TYPE";
                }
            } else {
                text = LocaleController.getString("PassportResidentialAddress", R.string.PassportResidentialAddress);
            }
            view.setTextAndValue(text, "", !last);
        } else if (type instanceof TLRPC.TL_secureValueTypePhone) {
            view.setTextAndValue(LocaleController.getString("PassportPhone", R.string.PassportPhone), "", !last);
        } else if (type instanceof TLRPC.TL_secureValueTypeEmail) {
            view.setTextAndValue(LocaleController.getString("PassportEmail", R.string.PassportEmail), "", !last);
        }
        if (currentActivityType == TYPE_MANAGE) {
            linearLayout2.addView(view, linearLayout2.getChildCount() - 5, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else {
            linearLayout2.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.SecureValueType documentsType = null;
                if (documentTypes != null) {
                    for (int a = 0, count = documentTypes.size(); a < count; a++) {
                        TLRPC.SecureValueType documentType = documentTypes.get(a);
                        if (getValueByType(documentType, false) != null || count == 1) {
                            documentsType = documentType;
                            break;
                        }
                    }
                }
                if (type instanceof TLRPC.TL_secureValueTypePersonalDetails || type instanceof TLRPC.TL_secureValueTypeAddress) {
                    if (documentsType == null && documentTypes != null && !documentTypes.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setPositiveButton(LocaleController.getString("Cancel", R.string.Cancel), null);

                        if (type instanceof TLRPC.TL_secureValueTypePersonalDetails) {
                            builder.setTitle(LocaleController.getString("PassportIdentityDocument", R.string.PassportIdentityDocument));
                        } else if (type instanceof TLRPC.TL_secureValueTypeAddress) {
                            builder.setTitle(LocaleController.getString("PassportAddress", R.string.PassportAddress));
                        }

                        ArrayList<String> strings = new ArrayList<>();
                        for (int a = 0, count = documentTypes.size(); a < count; a++) {
                            TLRPC.SecureValueType documentType = documentTypes.get(a);
                            if (documentType instanceof TLRPC.TL_secureValueTypeDriverLicense) {
                                strings.add(LocaleController.getString("PassportAddLicence", R.string.PassportAddLicence));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypePassport) {
                                strings.add(LocaleController.getString("PassportAddPassport", R.string.PassportAddPassport));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeInternalPassport) {
                                strings.add(LocaleController.getString("PassportAddInternalPassport", R.string.PassportAddInternalPassport));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                                strings.add(LocaleController.getString("PassportAddCard", R.string.PassportAddCard));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeUtilityBill) {
                                strings.add(LocaleController.getString("PassportAddBill", R.string.PassportAddBill));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeBankStatement) {
                                strings.add(LocaleController.getString("PassportAddBank", R.string.PassportAddBank));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeRentalAgreement) {
                                strings.add(LocaleController.getString("PassportAddAgreement", R.string.PassportAddAgreement));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypeTemporaryRegistration) {
                                strings.add(LocaleController.getString("PassportAddTemporaryRegistration", R.string.PassportAddTemporaryRegistration));
                            } else if (documentType instanceof TLRPC.TL_secureValueTypePassportRegistration) {
                                strings.add(LocaleController.getString("PassportAddPassportRegistration", R.string.PassportAddPassportRegistration));
                            }
                        }

                        builder.setItems(strings.toArray(new CharSequence[strings.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openTypeActivity(type, documentTypes.get(which), documentTypes);
                            }
                        });
                        showDialog(builder.create());
                        return;
                    }
                } else {
                    boolean phoneField;
                    if ((phoneField = (type instanceof TLRPC.TL_secureValueTypePhone)) || type instanceof TLRPC.TL_secureValueTypeEmail) {
                        final TLRPC.TL_secureValue value = getValueByType(type, false);
                        if (value != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    needShowProgress();
                                    deleteValueInternal(type, null, true, new Runnable() {
                                        @Override
                                        public void run() {
                                            needHideProgress();
                                        }
                                    }, new ErrorRunnable() {
                                        @Override
                                        public void onError(String error, String text) {
                                            needHideProgress();
                                        }
                                    });
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                            builder.setMessage(phoneField ? LocaleController.getString("PassportDeletePhoneAlert", R.string.PassportDeletePhoneAlert) : LocaleController.getString("PassportDeleteEmailAlert", R.string.PassportDeleteEmailAlert));
                            showDialog(builder.create());
                            return;
                        }
                    }
                }
                openTypeActivity(type, documentsType, documentTypes);
            }
        });
        typesViews.put(type, view);

        String text = null;
        String json = null;
        String documentJson = null;
        typesValues.put(type, new HashMap<String, String>());

        TLRPC.TL_secureValue value = getValueByType(type, false);
        if (value != null) {
            if (value.plain_data instanceof TLRPC.TL_securePlainEmail) {
                text = ((TLRPC.TL_securePlainEmail) value.plain_data).email;
            } else if (value.plain_data instanceof TLRPC.TL_securePlainPhone) {
                text = ((TLRPC.TL_securePlainPhone) value.plain_data).phone;
            } else if (value.data != null) {
                json = decryptData(value.data.data, decryptValueSecret(value.data.secret, value.data.data_hash), value.data.data_hash);
            }
        }
        TLRPC.SecureValueType documentsType = null;
        if (documentTypes != null && !documentTypes.isEmpty()) {
            for (int a = 0, count = documentTypes.size(); a < count; a++) {
                TLRPC.SecureValueType documentType = documentTypes.get(a);
                TLRPC.TL_secureValue documentValue = getValueByType(documentType, false);
                if (documentValue != null) {
                    if (documentValue.data != null) {
                        documentJson = decryptData(documentValue.data.data, decryptValueSecret(documentValue.data.secret, documentValue.data.data_hash), documentValue.data.data_hash);
                    }
                    documentsType = documentType;
                    break;
                }
            }
            if (documentsType == null) {
                documentsType = documentTypes.get(0);
            }
        }

        setTypeValue(type, text, json, documentsType, documentJson);
        return view;
    }

    private class EncryptionResult {
        byte[] fileSecret;
        byte[] decrypyedFileSecret;
        byte[] encryptedData;
        byte[] fileHash;
        SecureDocumentKey secureDocumentKey;

        public EncryptionResult(byte[] d, byte[] fs, byte[] dfs, byte[] fh, byte[] fk, byte[] fi) {
            encryptedData = d;
            fileSecret = fs;
            fileHash = fh;
            decrypyedFileSecret = dfs;
            secureDocumentKey = new SecureDocumentKey(fk, fi);
        }
    }

    private SecureDocumentKey getSecureDocumentKey(byte[] file_secret, byte[] file_hash) {
        byte[] decrypted_file_secret = decryptValueSecret(file_secret, file_hash);

        byte[] file_secret_hash = Utilities.computeSHA512(decrypted_file_secret, file_hash);
        byte[] file_key = new byte[32];
        System.arraycopy(file_secret_hash, 0, file_key, 0, 32);
        byte[] file_iv = new byte[16];
        System.arraycopy(file_secret_hash, 32, file_iv, 0, 16);

        return new SecureDocumentKey(file_key, file_iv);
    }

    private byte[] decryptSecret(byte[] secret, byte[] passwordHash) {
        if (secret == null || secret.length != 32) {
            return null;
        }
        byte[] key = new byte[32];
        System.arraycopy(passwordHash, 0, key, 0, 32);
        byte[] iv = new byte[16];
        System.arraycopy(passwordHash, 32, iv, 0, 16);

        byte[] decryptedSecret = new byte[32];
        System.arraycopy(secret, 0, decryptedSecret, 0, 32);
        Utilities.aesCbcEncryptionByteArraySafe(decryptedSecret, key, iv, 0, decryptedSecret.length, 0, 0);
        return decryptedSecret;
    }

    private byte[] decryptValueSecret(byte[] encryptedSecureValueSecret, byte[] hash) {
        if (encryptedSecureValueSecret == null || encryptedSecureValueSecret.length != 32 || hash == null || hash.length != 32) {
            return null;
        }
        byte[] key = new byte[32];
        System.arraycopy(saltedPassword, 0, key, 0, 32);
        byte[] iv = new byte[16];
        System.arraycopy(saltedPassword, 32, iv, 0, 16);

        byte[] decryptedSecret = new byte[32];
        System.arraycopy(secureSecret, 0, decryptedSecret, 0, 32);
        Utilities.aesCbcEncryptionByteArraySafe(decryptedSecret, key, iv, 0, decryptedSecret.length, 0, 0);
        if (!checkSecret(decryptedSecret, null)) {
            return null;
        }

        byte[] secret_hash = Utilities.computeSHA512(decryptedSecret, hash);
        byte[] file_secret_key = new byte[32];
        System.arraycopy(secret_hash, 0, file_secret_key, 0, 32);
        byte[] file_secret_iv = new byte[16];
        System.arraycopy(secret_hash, 32, file_secret_iv, 0, 16);

        byte[] result = new byte[32];
        System.arraycopy(encryptedSecureValueSecret, 0, result, 0, 32);
        Utilities.aesCbcEncryptionByteArraySafe(result, file_secret_key, file_secret_iv, 0, result.length, 0, 0);

        return result;
    }

    private EncryptionResult createSecureDocument(String path) {
        File file = new File(path);
        int length = (int) file.length();
        byte[] b = new byte[length];
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(path, "rws");
            f.readFully(b);
        } catch (Exception ignore) {

        }
        EncryptionResult result = encryptData(b);
        try {
            f.seek(0);
            f.write(result.encryptedData);
            f.close();
        } catch (Exception ignore) {

        }
        return result;
    }

    private String decryptData(byte[] data, byte[] file_secret, byte[] file_hash) {
        if (data == null || file_secret == null || file_secret.length != 32 || file_hash == null || file_hash.length != 32) {
            return null;
        }
        byte[] file_secret_hash = Utilities.computeSHA512(file_secret, file_hash);
        byte[] file_key = new byte[32];
        System.arraycopy(file_secret_hash, 0, file_key, 0, 32);
        byte[] file_iv = new byte[16];
        System.arraycopy(file_secret_hash, 32, file_iv, 0, 16);

        byte[] decryptedData = new byte[data.length];
        System.arraycopy(data, 0, decryptedData, 0, data.length);
        Utilities.aesCbcEncryptionByteArraySafe(decryptedData, file_key, file_iv, 0, decryptedData.length, 0, 0);

        byte[] hash = Utilities.computeSHA256(decryptedData);
        if (!Arrays.equals(hash, file_hash)) {
            return null;
        }

        int dataOffset = decryptedData[0] & 0xff;

        return new String(decryptedData, dataOffset, decryptedData.length - dataOffset);
    }

    private boolean checkSecret(byte[] secret, Long id) {
        if (secret == null || secret.length != 32) {
            return false;
        }
        int sum = 0;
        int a;
        for (a = 0; a < secret.length; a++) {
            sum += secret[a] & 0xff;
        }
        if (sum % 255 != 239) {
            return false;
        }

        if (id != null && Utilities.bytesToLong(Utilities.computeSHA256(secret)) != id) {
            return false;
        }

        return true;
    }

    private byte[] getRandomSecret() {
        byte[] secret = new byte[32];
        Utilities.random.nextBytes(secret);
        int sum = 0;
        int a;
        for (a = 0; a < secret.length; a++) {
            sum += secret[a] & 0xff;
        }
        sum = sum % 255;
        if (sum != 239) {
            sum = 239 - sum;

            a = Utilities.random.nextInt(32);
            int val = secret[a] & 0xff;
            val += sum;
            if (val < 255) {
                val = 255 + val;
            }
            secret[a] = (byte) (val % 255);
        }
        return secret;
    }

    private EncryptionResult encryptData(byte[] data) {
        byte[] file_secret = getRandomSecret();

        int extraLen = 32 + Utilities.random.nextInt(256 - 32 - 16);
        while ((data.length + extraLen) % 16 != 0) {
            extraLen++;
        }
        byte[] padding = new byte[extraLen];
        Utilities.random.nextBytes(padding);
        padding[0] = (byte) extraLen;
        byte[] paddedData = new byte[extraLen + data.length];
        System.arraycopy(padding, 0, paddedData, 0, extraLen);
        System.arraycopy(data, 0, paddedData, extraLen, data.length);

        byte[] file_hash = Utilities.computeSHA256(paddedData);
        byte[] file_secret_hash = Utilities.computeSHA512(file_secret, file_hash);
        byte[] file_key = new byte[32];
        System.arraycopy(file_secret_hash, 0, file_key, 0, 32);
        byte[] file_iv = new byte[16];
        System.arraycopy(file_secret_hash, 32, file_iv, 0, 16);

        Utilities.aesCbcEncryptionByteArraySafe(paddedData, file_key, file_iv, 0, paddedData.length, 0, 1);

        byte[] key = new byte[32];
        System.arraycopy(saltedPassword, 0, key, 0, 32);
        byte[] iv = new byte[16];
        System.arraycopy(saltedPassword, 32, iv, 0, 16);

        byte[] decryptedSecret = new byte[32];
        System.arraycopy(secureSecret, 0, decryptedSecret, 0, 32);
        Utilities.aesCbcEncryptionByteArraySafe(decryptedSecret, key, iv, 0, decryptedSecret.length, 0, 0);

        byte[] secret_hash = Utilities.computeSHA512(decryptedSecret, file_hash);
        byte[] file_secret_key = new byte[32];
        System.arraycopy(secret_hash, 0, file_secret_key, 0, 32);
        byte[] file_secret_iv = new byte[16];
        System.arraycopy(secret_hash, 32, file_secret_iv, 0, 16);

        byte[] encrypyed_file_secret = new byte[32];
        System.arraycopy(file_secret, 0, encrypyed_file_secret, 0, 32);
        Utilities.aesCbcEncryptionByteArraySafe(encrypyed_file_secret, file_secret_key, file_secret_iv, 0, encrypyed_file_secret.length, 0, 1);

        return new EncryptionResult(paddedData, encrypyed_file_secret, file_secret, file_hash, file_key, file_iv);
    }

    private void showAlertWithText(String title, String text) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.setTitle(title);
        builder.setMessage(text);
        showDialog(builder.create());
    }

    private void onPasscodeError(boolean clear) {
        if (getParentActivity() == null) {
            return;
        }
        Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(200);
        }
        if (clear) {
            inputFields[FIELD_PASSWORD].setText("");
        }
        AndroidUtilities.shakeView(inputFields[FIELD_PASSWORD], 2, 0);
    }

    private void startPhoneVerification(boolean checkPermissions, final String phone, Runnable finishRunnable, ErrorRunnable errorRunnable, final PassportActivityDelegate delegate) {
        TelephonyManager tm = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
        boolean simcardAvailable = tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
        boolean allowCall = true;
        if (getParentActivity() != null && Build.VERSION.SDK_INT >= 23 && simcardAvailable) {
            allowCall = getParentActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
            boolean allowSms = getParentActivity().checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
            if (checkPermissions) {
                permissionsItems.clear();
                if (!allowCall) {
                    permissionsItems.add(Manifest.permission.READ_PHONE_STATE);
                }
                /*
                if (!allowSms) {
                    permissionsItems.add(Manifest.permission.RECEIVE_SMS);
                    if (Build.VERSION.SDK_INT >= 23) {
                        permissionsItems.add(Manifest.permission.READ_SMS);
                    }
                }*/
                if (!permissionsItems.isEmpty()) {
                    if (getParentActivity().shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) || getParentActivity().shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        if (permissionsItems.size() == 2) {
                            builder.setMessage(LocaleController.getString("AllowReadCallAndSms", R.string.AllowReadCallAndSms));
                        } else if (!allowSms) {
                            builder.setMessage(LocaleController.getString("AllowReadSms", R.string.AllowReadSms));
                        } else {
                            builder.setMessage(LocaleController.getString("AllowReadCall", R.string.AllowReadCall));
                        }
                        permissionsDialog = showDialog(builder.create());
                    } else {
                        getParentActivity().requestPermissions(permissionsItems.toArray(new String[permissionsItems.size()]), 6);
                    }
                    pendingPhone = phone;
                    pendingErrorRunnable = errorRunnable;
                    pendingFinishRunnable = finishRunnable;
                    pendingDelegate = delegate;
                    return;
                }
            }
        }
        final TLRPC.TL_account_sendVerifyPhoneCode req = new TLRPC.TL_account_sendVerifyPhoneCode();
        req.phone_number = phone;
        req.allow_flashcall = simcardAvailable && allowCall;
        if (req.allow_flashcall) {
            try {
                @SuppressLint("HardwareIds")
                String number = tm.getLine1Number();
                if (!TextUtils.isEmpty(number)) {
                    req.current_number = phone.contains(number) || number.contains(phone);
                    if (!req.current_number) {
                        req.allow_flashcall = false;
                    }
                } else {
                    req.current_number = false;
                }
            } catch (Exception e) {
                req.allow_flashcall = false;
                FileLog.e(e);
            }
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            HashMap<String, String> values = new HashMap<>();
                            values.put("phone", phone);
                            PassportActivity activity = new PassportActivity(TYPE_PHONE_VERIFICATION, currentForm, currentPassword, currentType, null, null, null, values);
                            activity.currentAccount = currentAccount;
                            activity.saltedPassword = saltedPassword;
                            activity.secureSecret = secureSecret;
                            activity.delegate = delegate;
                            activity.currentPhoneVerification = (TLRPC.TL_auth_sentCode) response;
                            presentFragment(activity, true);
                        } else {
                            AlertsCreator.processError(currentAccount, error, PassportActivity.this, req, phone);
                        }
                    }
                });
            }
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    private void updatePasswordInterface() {
        if (noPasswordImageView == null) {
            return;
        }
        if (currentPassword == null || usingSavedPassword != 0) {
            noPasswordImageView.setVisibility(View.GONE);
            noPasswordTextView.setVisibility(View.GONE);
            noPasswordSetTextView.setVisibility(View.GONE);
            passwordAvatarContainer.setVisibility(View.GONE);
            inputFieldContainers[FIELD_PASSWORD].setVisibility(View.GONE);
            doneItem.setVisibility(View.GONE);
            passwordForgotButton.setVisibility(View.GONE);
            passwordInfoRequestTextView.setVisibility(View.GONE);
            passwordRequestTextView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else if (currentPassword instanceof TLRPC.TL_account_noPassword) {
            passwordRequestTextView.setVisibility(View.VISIBLE);

            noPasswordImageView.setVisibility(View.VISIBLE);
            noPasswordTextView.setVisibility(View.VISIBLE);
            noPasswordSetTextView.setVisibility(View.VISIBLE);

            passwordAvatarContainer.setVisibility(View.GONE);
            inputFieldContainers[FIELD_PASSWORD].setVisibility(View.GONE);
            doneItem.setVisibility(View.GONE);
            passwordForgotButton.setVisibility(View.GONE);
            passwordInfoRequestTextView.setVisibility(View.GONE);
            passwordRequestTextView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 25, 0, 0));
            emptyView.setVisibility(View.GONE);
        } else {
            passwordRequestTextView.setVisibility(View.VISIBLE);

            noPasswordImageView.setVisibility(View.GONE);
            noPasswordTextView.setVisibility(View.GONE);
            noPasswordSetTextView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            passwordAvatarContainer.setVisibility(View.VISIBLE);
            inputFieldContainers[FIELD_PASSWORD].setVisibility(View.VISIBLE);
            doneItem.setVisibility(View.VISIBLE);
            passwordForgotButton.setVisibility(View.VISIBLE);
            passwordInfoRequestTextView.setVisibility(View.VISIBLE);
            passwordRequestTextView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0));

            if (inputFields != null) {
                if (currentPassword != null && !TextUtils.isEmpty(currentPassword.hint)) {
                    inputFields[FIELD_PASSWORD].setHint(currentPassword.hint);
                } else {
                    inputFields[FIELD_PASSWORD].setHint(LocaleController.getString("LoginPassword", R.string.LoginPassword));
                }
            }
        }
    }

    private void showEditDoneProgress(final boolean animateDoneItem, final boolean show) {
        if (doneItemAnimation != null) {
            doneItemAnimation.cancel();
        }
        if (animateDoneItem && doneItem != null) {
            doneItemAnimation = new AnimatorSet();
            if (show) {
                progressView.setVisibility(View.VISIBLE);
                doneItem.setEnabled(false);
                doneItemAnimation.playTogether(
                        ObjectAnimator.ofFloat(doneItem.getImageView(), "scaleX", 0.1f),
                        ObjectAnimator.ofFloat(doneItem.getImageView(), "scaleY", 0.1f),
                        ObjectAnimator.ofFloat(doneItem.getImageView(), "alpha", 0.0f),
                        ObjectAnimator.ofFloat(progressView, "scaleX", 1.0f),
                        ObjectAnimator.ofFloat(progressView, "scaleY", 1.0f),
                        ObjectAnimator.ofFloat(progressView, "alpha", 1.0f));
            } else {
                doneItem.getImageView().setVisibility(View.VISIBLE);
                doneItem.setEnabled(true);
                doneItemAnimation.playTogether(
                        ObjectAnimator.ofFloat(progressView, "scaleX", 0.1f),
                        ObjectAnimator.ofFloat(progressView, "scaleY", 0.1f),
                        ObjectAnimator.ofFloat(progressView, "alpha", 0.0f),
                        ObjectAnimator.ofFloat(doneItem.getImageView(), "scaleX", 1.0f),
                        ObjectAnimator.ofFloat(doneItem.getImageView(), "scaleY", 1.0f),
                        ObjectAnimator.ofFloat(doneItem.getImageView(), "alpha", 1.0f));
            }
            doneItemAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (doneItemAnimation != null && doneItemAnimation.equals(animation)) {
                        if (!show) {
                            progressView.setVisibility(View.INVISIBLE);
                        } else {
                            doneItem.getImageView().setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (doneItemAnimation != null && doneItemAnimation.equals(animation)) {
                        doneItemAnimation = null;
                    }
                }
            });
            doneItemAnimation.setDuration(150);
            doneItemAnimation.start();
        } else if (acceptTextView != null) {
            doneItemAnimation = new AnimatorSet();
            if (show) {
                progressViewButton.setVisibility(View.VISIBLE);
                bottomLayout.setEnabled(false);
                doneItemAnimation.playTogether(
                        ObjectAnimator.ofFloat(acceptTextView, "scaleX", 0.1f),
                        ObjectAnimator.ofFloat(acceptTextView, "scaleY", 0.1f),
                        ObjectAnimator.ofFloat(acceptTextView, "alpha", 0.0f),
                        ObjectAnimator.ofFloat(progressViewButton, "scaleX", 1.0f),
                        ObjectAnimator.ofFloat(progressViewButton, "scaleY", 1.0f),
                        ObjectAnimator.ofFloat(progressViewButton, "alpha", 1.0f));
            } else {
                acceptTextView.setVisibility(View.VISIBLE);
                bottomLayout.setEnabled(true);
                doneItemAnimation.playTogether(
                        ObjectAnimator.ofFloat(progressViewButton, "scaleX", 0.1f),
                        ObjectAnimator.ofFloat(progressViewButton, "scaleY", 0.1f),
                        ObjectAnimator.ofFloat(progressViewButton, "alpha", 0.0f),
                        ObjectAnimator.ofFloat(acceptTextView, "scaleX", 1.0f),
                        ObjectAnimator.ofFloat(acceptTextView, "scaleY", 1.0f),
                        ObjectAnimator.ofFloat(acceptTextView, "alpha", 1.0f));

            }
            doneItemAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (doneItemAnimation != null && doneItemAnimation.equals(animation)) {
                        if (!show) {
                            progressViewButton.setVisibility(View.INVISIBLE);
                        } else {
                            acceptTextView.setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (doneItemAnimation != null && doneItemAnimation.equals(animation)) {
                        doneItemAnimation = null;
                    }
                }
            });
            doneItemAnimation.setDuration(150);
            doneItemAnimation.start();
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.FileDidUpload) {
            final String location = (String) args[0];
            SecureDocument document = uploadingDocuments.get(location);
            if (document != null) {
                document.inputFile = (TLRPC.TL_inputFile) args[1];
                uploadingDocuments.remove(location);
                if (uploadingDocuments.isEmpty()) {
                    if (doneItem != null) {
                        doneItem.setEnabled(true);
                        doneItem.setAlpha(1.0f);
                    }
                }
                if (documentsCells != null) {
                    SecureDocumentCell cell = documentsCells.get(document);
                    if (cell != null) {
                        cell.updateButtonState(true);
                    }
                }
                errorsValues.remove("files_all");
                if (bottomCell != null && !TextUtils.isEmpty(noAllDocumentsErrorText)) {
                    bottomCell.setText(noAllDocumentsErrorText);
                }
            }
        } else if (id == NotificationCenter.FileDidFailUpload) {

        } else if (id == NotificationCenter.didSetTwoStepPassword) {
            if (args != null && args.length > 0) {
                if (args[7] != null && inputFields[FIELD_PASSWORD] != null) {
                    inputFields[FIELD_PASSWORD].setText((String) args[7]);
                }
                if (args[6] == null) {
                    currentPassword = new TLRPC.TL_account_password();
                    currentPassword.current_salt = (byte[]) args[1];
                    currentPassword.new_secure_salt = (byte[]) args[2];
                    currentPassword.secure_random = (byte[]) args[3];
                    currentPassword.has_recovery = !TextUtils.isEmpty((String) args[4]);
                    currentPassword.hint = (String) args[5];

                    if (inputFields[FIELD_PASSWORD] != null && inputFields[FIELD_PASSWORD].length() > 0) {
                        usingSavedPassword = 2;
                    }
                }
            } else {
                currentPassword = null;
                loadPasswordInfo();
            }
            updatePasswordInterface();
        } else if (id == NotificationCenter.didRemovedTwoStepPassword) {

        }
    }

    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (presentAfterAnimation != null) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    presentFragment(presentAfterAnimation, true);
                    presentAfterAnimation = null;
                }
            });
        }
        if (currentActivityType == TYPE_PASSWORD) {
            if (isOpen) {
                if (inputFieldContainers[FIELD_PASSWORD].getVisibility() == View.VISIBLE) {
                    inputFields[FIELD_PASSWORD].requestFocus();
                    AndroidUtilities.showKeyboard(inputFields[FIELD_PASSWORD]);
                }
                if (usingSavedPassword == 2) {
                    onPasswordDone(false);
                }
            }
        } else if (currentActivityType == TYPE_PHONE_VERIFICATION) {
            if (isOpen) {
                views[currentViewNum].onShow();
            }
        } else if (currentActivityType == TYPE_EMAIL) {
            if (isOpen) {
                inputFields[FIELD_EMAIL].requestFocus();
                AndroidUtilities.showKeyboard(inputFields[FIELD_EMAIL]);
            }
        } else if (currentActivityType == TYPE_EMAIL_VERIFICATION) {
            if (isOpen) {
                inputFields[FIELD_EMAIL].requestFocus();
                AndroidUtilities.showKeyboard(inputFields[FIELD_EMAIL]);
            }
        } else if (currentActivityType == TYPE_ADDRESS || currentActivityType == TYPE_IDENTITY) {
            if (Build.VERSION.SDK_INT >= 21) {
                createChatAttachView();
            }
        }
    }

    private void showAttachmentError() {
        if (getParentActivity() == null) {
            return;
        }
        Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("UnsupportedAttachment", R.string.UnsupportedAttachment), Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0 || requestCode == 2) {
                createChatAttachView();
                if (chatAttachAlert != null) {
                    chatAttachAlert.onActivityResultFragment(requestCode, data, currentPicturePath);
                }
                currentPicturePath = null;
            } else if (requestCode == 1) {
                if (data == null || data.getData() == null) {
                    showAttachmentError();
                    return;
                }
                ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
                SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
                info.uri = data.getData();
                photos.add(info);
                processSelectedFiles(photos);
            }
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if ((currentActivityType == TYPE_IDENTITY || currentActivityType == TYPE_ADDRESS) && chatAttachAlert != null) {
            if (requestCode == 17 && chatAttachAlert != null) {
                chatAttachAlert.checkCamera(false);
            } else if (requestCode == 21) {
                if (getParentActivity() == null) {
                    return;
                }
                if (grantResults != null && grantResults.length != 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setMessage(LocaleController.getString("PermissionNoAudioVideo", R.string.PermissionNoAudioVideo));
                    builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                                getParentActivity().startActivity(intent);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    });
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                    builder.show();
                }
            } else if (requestCode == 19 && grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                processSelectedAttach(attach_photo);
            } else if (requestCode == 22 && grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (scanDocumentCell != null) {
                    scanDocumentCell.callOnClick();
                }
            }
        } else if (currentActivityType == TYPE_PHONE && requestCode == 6) {
            startPhoneVerification(false, pendingPhone, pendingFinishRunnable, pendingErrorRunnable, pendingDelegate);
        }
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (currentPicturePath != null) {
            args.putString("path", currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        currentPicturePath = args.getString("path");
    }

    @Override
    public boolean onBackPressed() {
        if (currentActivityType == TYPE_PHONE_VERIFICATION) {
            views[currentViewNum].onBackPressed();
            for (int a = 0; a < views.length; a++) {
                if (views[a] != null) {
                    views[a].onDestroyActivity();
                }
            }
        } else if (currentActivityType == TYPE_REQUEST || currentActivityType == TYPE_PASSWORD) {
            callCallback(false);
        } else if (currentActivityType == TYPE_IDENTITY || currentActivityType == TYPE_ADDRESS) {
            return !checkDiscard();
        }
        return true;
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        if (currentActivityType == TYPE_PHONE) {
            if (Build.VERSION.SDK_INT >= 23 && dialog == permissionsDialog && !permissionsItems.isEmpty()) {
                getParentActivity().requestPermissions(permissionsItems.toArray(new String[permissionsItems.size()]), 6);
            }
        }
    }

    //-----phone verification
    private String pendingPhone;
    private Runnable pendingFinishRunnable;
    private ErrorRunnable pendingErrorRunnable;
    private PassportActivityDelegate pendingDelegate;
    private int currentViewNum;
    private SlideView[] views;
    private AlertDialog progressDialog;
    private Dialog permissionsDialog;
    private ArrayList<String> permissionsItems;

    public void needShowProgress() {
        if (getParentActivity() == null || getParentActivity().isFinishing() || progressDialog != null) {
            return;
        }
        progressDialog = new AlertDialog(getParentActivity(), 1);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void needHideProgress() {
        if (progressDialog == null) {
            return;
        }
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            FileLog.e(e);
        }
        progressDialog = null;
    }

    public void setPage(int page, boolean animated, Bundle params) {
        if (page == 3) {
            doneItem.setVisibility(View.GONE);
        }
        final SlideView outView = views[currentViewNum];
        final SlideView newView = views[page];
        currentViewNum = page;

        newView.setParams(params, false);
        newView.onShow();

        if (animated) {
            newView.setTranslationX(AndroidUtilities.displaySize.x);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.setDuration(300);
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(outView, "translationX", -AndroidUtilities.displaySize.x),
                    ObjectAnimator.ofFloat(newView, "translationX", 0));
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    newView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    outView.setVisibility(View.GONE);
                    outView.setX(0);
                }
            });
            animatorSet.start();
        } else {
            newView.setTranslationX(0);
            newView.setVisibility(View.VISIBLE);
            if (outView != newView) {
                outView.setVisibility(View.GONE);
            }
        }
    }

    private void fillNextCodeParams(Bundle params, TLRPC.TL_auth_sentCode res, boolean animated) {
        params.putString("phoneHash", res.phone_code_hash);
        if (res.next_type instanceof TLRPC.TL_auth_codeTypeCall) {
            params.putInt("nextType", 4);
        } else if (res.next_type instanceof TLRPC.TL_auth_codeTypeFlashCall) {
            params.putInt("nextType", 3);
        } else if (res.next_type instanceof TLRPC.TL_auth_codeTypeSms) {
            params.putInt("nextType", 2);
        }
        if (res.timeout == 0) {
            res.timeout = 60;
        }
        params.putInt("timeout", res.timeout * 1000);
        if (res.type instanceof TLRPC.TL_auth_sentCodeTypeCall) {
            params.putInt("type", 4);
            params.putInt("length", res.type.length);
            setPage(2, animated, params);
        } else if (res.type instanceof TLRPC.TL_auth_sentCodeTypeFlashCall) {
            params.putInt("type", 3);
            params.putString("pattern", res.type.pattern);
            setPage(1, animated, params);
        } else if (res.type instanceof TLRPC.TL_auth_sentCodeTypeSms) {
            params.putInt("type", 2);
            params.putInt("length", res.type.length);
            setPage(0, animated, params);
        }
    }

    private void openAttachMenu() {
        if (getParentActivity() == null) {
            return;
        }
        if (uploadingFileType == UPLOADING_TYPE_DOCUMENTS && documents.size() >= 20) {
            showAlertWithText(LocaleController.getString("AppName", R.string.AppName), LocaleController.formatString("PassportUploadMaxReached", R.string.PassportUploadMaxReached, LocaleController.formatPluralString("Files", 20)));
            return;
        }
        createChatAttachView();
        chatAttachAlert.setOpenWithFrontFaceCamera(uploadingFileType == UPLOADING_TYPE_SELFIE);
        chatAttachAlert.setMaxSelectedPhotos(uploadingFileType == UPLOADING_TYPE_DOCUMENTS ? 20 - documents.size() : 1);
        chatAttachAlert.loadGalleryPhotos();
        if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
            AndroidUtilities.hideKeyboard(fragmentView.findFocus());
        }
        chatAttachAlert.init();
        showDialog(chatAttachAlert);
    }

    private void createChatAttachView() {
        if (getParentActivity() == null) {
            return;
        }
        if (chatAttachAlert == null) {
            chatAttachAlert = new ChatAttachAlert(getParentActivity(), this);
            chatAttachAlert.setDelegate(new ChatAttachAlert.ChatAttachViewDelegate() {

                @Override
                public void didPressedButton(int button) {
                    if (getParentActivity() == null || chatAttachAlert == null) {
                        return;
                    }
                    if (button == 8 || button == 7) {
                        if (button != 8) {
                            chatAttachAlert.dismiss();
                        }
                        HashMap<Object, Object> selectedPhotos = chatAttachAlert.getSelectedPhotos();
                        ArrayList<Object> selectedPhotosOrder = chatAttachAlert.getSelectedPhotosOrder();
                        if (!selectedPhotos.isEmpty()) {
                            ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
                            for (int a = 0; a < selectedPhotosOrder.size(); a++) {
                                MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) selectedPhotos.get(selectedPhotosOrder.get(a));

                                SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
                                if (photoEntry.imagePath != null) {
                                    info.path = photoEntry.imagePath;
                                } else if (photoEntry.path != null) {
                                    info.path = photoEntry.path;
                                }
                                photos.add(info);
                                photoEntry.reset();
                            }
                            processSelectedFiles(photos);
                        }
                        return;
                    } else if (chatAttachAlert != null) {
                        chatAttachAlert.dismissWithButtonClick(button);
                    }
                    processSelectedAttach(button);
                }

                @Override
                public View getRevealView() {
                    return null;
                }

                @Override
                public void didSelectBot(TLRPC.User user) {

                }

                @Override
                public void onCameraOpened() {
                    AndroidUtilities.hideKeyboard(fragmentView.findFocus());
                }

                @Override
                public boolean allowGroupPhotos() {
                    return false;
                }
            });
        }
    }

    private void processSelectedAttach(int which) {
        if (which == attach_photo) {
            if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                getParentActivity().requestPermissions(new String[]{Manifest.permission.CAMERA}, 19);
                return;
            }
            try {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File image = AndroidUtilities.generatePicturePath();
                if (image != null) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", image));
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                    }
                    currentPicturePath = image.getAbsolutePath();
                }
                startActivityForResult(takePictureIntent, 0);
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else if (which == attach_gallery) {
            if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
                return;
            }
            PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(false, false, false, null);
            fragment.setCurrentAccount(currentAccount);
            fragment.setMaxSelectedPhotos(uploadingFileType == UPLOADING_TYPE_DOCUMENTS ? 20 - documents.size() : 1);
            fragment.setAllowSearchImages(false);
            fragment.setDelegate(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {
                @Override
                public void didSelectPhotos(ArrayList<SendMessagesHelper.SendingMediaInfo> photos) {
                    processSelectedFiles(photos);
                }

                @Override
                public void startPhotoSelectActivity() {
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, 1);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            });
            presentFragment(fragment);
        } else if (which == attach_document) {
            if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
                return;
            }
            DocumentSelectActivity fragment = new DocumentSelectActivity();
            fragment.setCurrentAccount(currentAccount);
            fragment.setCanSelectOnlyImageFiles(true);
            fragment.setMaxSelectedFiles(uploadingFileType == UPLOADING_TYPE_DOCUMENTS ? 20 - documents.size() : 1);
            fragment.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {
                @Override
                public void didSelectFiles(DocumentSelectActivity activity, ArrayList<String> files) {
                    activity.finishFragment();
                    ArrayList<SendMessagesHelper.SendingMediaInfo> arrayList = new ArrayList<>();
                    for (int a = 0, count = files.size(); a < count; a++) {
                        SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
                        info.path = files.get(a);
                        arrayList.add(info);
                    }
                    processSelectedFiles(arrayList);
                }

                @Override
                public void startDocumentSelectActivity() {
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        if (Build.VERSION.SDK_INT >= 18) {
                            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        }
                        photoPickerIntent.setType("*/*");
                        startActivityForResult(photoPickerIntent, 21);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            });
            presentFragment(fragment);
        }
    }

    private void fillInitialValues() {
        if (initialValues != null) {
            return;
        }
        initialValues = getCurrentValues();
    }

    private String getCurrentValues() {
        StringBuilder values = new StringBuilder();
        for (int a = 0; a < inputFields.length; a++) {
            values.append(inputFields[a].getText()).append(",");
        }
        for (int a = 0, count = documents.size(); a < count; a++) {
            values.append(documents.get(a).secureFile.id);
        }
        if (selfieDocument != null) {
            values.append(selfieDocument.secureFile.id);
        }
        return values.toString();
    }

    private boolean isHasNotAnyChanges() {
        return initialValues == null || initialValues.equals(getCurrentValues());
    }

    private boolean checkDiscard() {
        if (isHasNotAnyChanges()) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setPositiveButton(LocaleController.getString("PassportDiscard", R.string.PassportDiscard), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishFragment();
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.setTitle(LocaleController.getString("DiscardChanges", R.string.DiscardChanges));
        builder.setMessage(LocaleController.getString("PassportDiscardChanges", R.string.PassportDiscardChanges));
        showDialog(builder.create());
        return true;
    }

    private void processSelectedFiles(final ArrayList<SendMessagesHelper.SendingMediaInfo> photos) {
        if (photos.isEmpty()) {
            return;
        }
        final boolean needRecoginze;
        if (uploadingFileType == UPLOADING_TYPE_SELFIE) {
            needRecoginze = false;
        } else if (currentType instanceof TLRPC.TL_secureValueTypePersonalDetails) {
            boolean allFieldsAreEmpty = true;
            for (int a = 0; a < inputFields.length; a++) {
                if (a == FIELD_CITIZENSHIP || a == FIELD_EXPIRE || a == FIELD_GENDER || a == FIELD_RESIDENCE) {
                    continue;
                }
                if (inputFields[a].length() > 0) {
                    allFieldsAreEmpty = false;
                    break;
                }
            }
            needRecoginze = allFieldsAreEmpty;
        } else {
            needRecoginze = false;
        }
        final int type = uploadingFileType;
        Utilities.globalQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                boolean didRecognizeSuccessfully = false;
                for (int a = 0, count = Math.min(uploadingFileType == UPLOADING_TYPE_DOCUMENTS ? 20 : 1, photos.size()); a < count; a++) {
                    SendMessagesHelper.SendingMediaInfo info = photos.get(a);
                    Bitmap bitmap = ImageLoader.loadBitmap(info.path, info.uri, 2048, 2048, false);
                    if (bitmap == null) {
                        continue;
                    }
                    TLRPC.PhotoSize size = ImageLoader.scaleAndSaveImage(bitmap, 2048, 2048, 89, false, 320, 320);
                    if (size == null) {
                        continue;
                    }
                    TLRPC.TL_secureFile secureFile = new TLRPC.TL_secureFile();
                    secureFile.dc_id = (int) size.location.volume_id;
                    secureFile.id = size.location.local_id;
                    secureFile.date = (int) (System.currentTimeMillis() / 1000);

                    final SecureDocument document = delegate.saveFile(secureFile);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (uploadingFileType == UPLOADING_TYPE_SELFIE) {
                                if (selfieDocument != null) {
                                    SecureDocumentCell cell = documentsCells.remove(selfieDocument);
                                    if (cell != null) {
                                        selfieLayout.removeView(cell);
                                    }
                                    selfieDocument = null;
                                }
                            } else if (uploadingFileType == UPLOADING_TYPE_FRONT) {
                                if (frontDocument != null) {
                                    SecureDocumentCell cell = documentsCells.remove(frontDocument);
                                    if (cell != null) {
                                        frontLayout.removeView(cell);
                                    }
                                    frontDocument = null;
                                }
                            } else if (uploadingFileType == UPLOADING_TYPE_REVERSE) {
                                if (reverseDocument != null) {
                                    SecureDocumentCell cell = documentsCells.remove(reverseDocument);
                                    if (cell != null) {
                                        reverseLayout.removeView(cell);
                                    }
                                    reverseDocument = null;
                                }
                            } else if (uploadingFileType == UPLOADING_TYPE_DOCUMENTS) {
                                if (documents.size() >= 20) {
                                    return;
                                }
                            }
                            uploadingDocuments.put(document.path, document);
                            doneItem.setEnabled(false);
                            doneItem.setAlpha(0.5f);
                            FileLoader.getInstance(currentAccount).uploadFile(document.path, false, true, ConnectionsManager.FileTypePhoto);
                            addDocumentView(document, type);
                            updateUploadText(type);
                        }
                    });

                    if (needRecoginze && !didRecognizeSuccessfully) {
                        try {
                            final MrzRecognizer.Result result = MrzRecognizer.recognize(bitmap, currentDocumentsType instanceof TLRPC.TL_secureValueTypeDriverLicense);
                            if (result != null) {
                                didRecognizeSuccessfully = true;
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result.type == MrzRecognizer.Result.TYPE_ID) {
                                            if (!(currentDocumentsType instanceof TLRPC.TL_secureValueTypeIdentityCard)) {
                                                for (int a = 0, count = availableDocumentTypes.size(); a < count; a++) {
                                                    TLRPC.SecureValueType type = availableDocumentTypes.get(a);
                                                    if (type instanceof TLRPC.TL_secureValueTypeIdentityCard) {
                                                        currentDocumentsType = type;
                                                        updateInterfaceStringsForDocumentType();
                                                        break;
                                                    }
                                                }
                                            }
                                        } else if (result.type == MrzRecognizer.Result.TYPE_PASSPORT) {
                                            if (!(currentDocumentsType instanceof TLRPC.TL_secureValueTypePassport)) {
                                                for (int a = 0, count = availableDocumentTypes.size(); a < count; a++) {
                                                    TLRPC.SecureValueType type = availableDocumentTypes.get(a);
                                                    if (type instanceof TLRPC.TL_secureValueTypePassport) {
                                                        currentDocumentsType = type;
                                                        updateInterfaceStringsForDocumentType();
                                                        break;
                                                    }
                                                }
                                            }
                                        } else if (result.type == MrzRecognizer.Result.TYPE_INTERNAL_PASSPORT) {
                                            if (!(currentDocumentsType instanceof TLRPC.TL_secureValueTypeInternalPassport)) {
                                                for (int a = 0, count = availableDocumentTypes.size(); a < count; a++) {
                                                    TLRPC.SecureValueType type = availableDocumentTypes.get(a);
                                                    if (type instanceof TLRPC.TL_secureValueTypeInternalPassport) {
                                                        currentDocumentsType = type;
                                                        updateInterfaceStringsForDocumentType();
                                                        break;
                                                    }
                                                }
                                            }
                                        } else if (result.type == MrzRecognizer.Result.TYPE_DRIVER_LICENSE) {
                                            if (!(currentDocumentsType instanceof TLRPC.TL_secureValueTypeDriverLicense)) {
                                                for (int a = 0, count = availableDocumentTypes.size(); a < count; a++) {
                                                    TLRPC.SecureValueType type = availableDocumentTypes.get(a);
                                                    if (type instanceof TLRPC.TL_secureValueTypeDriverLicense) {
                                                        currentDocumentsType = type;
                                                        updateInterfaceStringsForDocumentType();
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (!TextUtils.isEmpty(result.firstName)) {
                                            inputFields[FIELD_NAME].setText(result.firstName);
                                        }
                                        if (!TextUtils.isEmpty(result.lastName)) {
                                            inputFields[FIELD_SURNAME].setText(result.lastName);
                                        }
                                        if (!TextUtils.isEmpty(result.number)) {
                                            inputFields[FIELD_CARDNUMBER].setText(result.number);
                                        }
                                        if (result.gender != MrzRecognizer.Result.GENDER_UNKNOWN) {
                                            switch (result.gender) {
                                                case MrzRecognizer.Result.GENDER_MALE:
                                                    currentGender = "male";
                                                    inputFields[FIELD_GENDER].setText(LocaleController.getString("PassportMale", R.string.PassportMale));
                                                    break;
                                                case MrzRecognizer.Result.GENDER_FEMALE:
                                                    currentGender = "female";
                                                    inputFields[FIELD_GENDER].setText(LocaleController.getString("PassportFemale", R.string.PassportFemale));
                                                    break;
                                            }
                                        }
                                        if (!TextUtils.isEmpty(result.nationality)) {
                                            currentCitizeship = result.nationality;
                                            String country = languageMap.get(currentCitizeship);
                                            if (country != null) {
                                                inputFields[FIELD_CITIZENSHIP].setText(country);
                                            }
                                        }
                                        if (!TextUtils.isEmpty(result.issuingCountry)) {
                                            currentResidence = result.issuingCountry;
                                            String country = languageMap.get(currentResidence);
                                            if (country != null) {
                                                inputFields[FIELD_RESIDENCE].setText(country);
                                            }
                                        }
                                        if (result.birthDay > 0 && result.birthMonth > 0 && result.birthYear > 0) {
                                            inputFields[FIELD_BIRTHDAY].setText(String.format(Locale.US, "%02d.%02d.%d", result.birthDay, result.birthMonth, result.birthYear));
                                        }
                                        if (result.expiryDay > 0 && result.expiryMonth > 0 && result.expiryYear > 0) {
                                            currentExpireDate[0] = result.expiryYear;
                                            currentExpireDate[1] = result.expiryMonth;
                                            currentExpireDate[2] = result.expiryDay;
                                            inputFields[FIELD_EXPIRE].setText(String.format(Locale.US, "%02d.%02d.%d", result.expiryDay, result.expiryMonth, result.expiryYear));
                                        } else {
                                            currentExpireDate[0] = currentExpireDate[1] = currentExpireDate[2] = 0;
                                            inputFields[FIELD_EXPIRE].setText(LocaleController.getString("PassportNoExpireDate", R.string.PassportNoExpireDate));
                                        }
                                    }
                                });
                            }
                        } catch (Throwable e) {
                            FileLog.e(e);
                        }
                    }

                }
                SharedConfig.saveConfig();
            }
        });
    }

    public void setNeedActivityResult(boolean needActivityResult) {
        this.needActivityResult = needActivityResult;
    }

    public class PhoneConfirmationView extends SlideView implements NotificationCenter.NotificationCenterDelegate {

        private String phone;
        private String phoneHash;
        private EditTextBoldCursor codeField;
        private TextView confirmTextView;
        private TextView timeText;
        private TextView problemText;
        private Bundle currentParams;
        private ProgressView progressView;

        private Timer timeTimer;
        private Timer codeTimer;
        private int openTime;
        private final Object timerSync = new Object();
        private int time = 60000;
        private int codeTime = 15000;
        private double lastCurrentTime;
        private double lastCodeTime;
        private boolean ignoreOnTextChange;
        private boolean waitingForEvent;
        private boolean nextPressed;
        private String lastError = "";
        private int verificationType;
        private int nextType;
        private String pattern = "*";
        private int length;
        private int timeout;

        private class ProgressView extends View {

            private Paint paint = new Paint();
            private Paint paint2 = new Paint();
            private float progress;

            public ProgressView(Context context) {
                super(context);
                paint.setColor(Theme.getColor(Theme.key_login_progressInner));
                paint2.setColor(Theme.getColor(Theme.key_login_progressOuter));
            }

            public void setProgress(float value) {
                progress = value;
                invalidate();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int start = (int) (getMeasuredWidth() * progress);
                canvas.drawRect(0, 0, start, getMeasuredHeight(), paint2);
                canvas.drawRect(start, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
            }
        }

        public PhoneConfirmationView(Context context, final int type) {
            super(context);

            verificationType = type;
            setOrientation(VERTICAL);

            confirmTextView = new TextView(context);
            confirmTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
            confirmTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            confirmTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

            if (verificationType == 3) {
                FrameLayout frameLayout = new FrameLayout(context);

                ImageView imageView = new ImageView(context);
                imageView.setImageResource(R.drawable.phone_activate);
                if (LocaleController.isRTL) {
                    frameLayout.addView(imageView, LayoutHelper.createFrame(64, 76, Gravity.LEFT | Gravity.CENTER_VERTICAL, 2, 2, 0, 0));
                    frameLayout.addView(confirmTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 64 + 18, 0, 0, 0));
                } else {
                    frameLayout.addView(confirmTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 0, 64 + 18, 0));
                    frameLayout.addView(imageView, LayoutHelper.createFrame(64, 76, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 2, 0, 2));
                }
                addView(frameLayout, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
            } else {
                addView(confirmTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT));
            }

            codeField = new EditTextBoldCursor(context);
            codeField.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            codeField.setHint(LocaleController.getString("Code", R.string.Code));
            codeField.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            codeField.setCursorSize(AndroidUtilities.dp(20));
            codeField.setCursorWidth(1.5f);
            codeField.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            codeField.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
            codeField.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            codeField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            codeField.setInputType(InputType.TYPE_CLASS_PHONE);
            codeField.setMaxLines(1);
            codeField.setPadding(0, 0, 0, 0);
            addView(codeField, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));
            codeField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (ignoreOnTextChange) {
                        return;
                    }
                    if (length != 0 && codeField.length() == length) {
                        onNextPressed();
                    }
                }
            });
            codeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_NEXT) {
                        onNextPressed();
                        return true;
                    }
                    return false;
                }
            });
            if (verificationType == 3) {
                codeField.setEnabled(false);
                codeField.setInputType(InputType.TYPE_NULL);
                codeField.setVisibility(GONE);
            }

            timeText = new TextView(context);
            timeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            timeText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
            timeText.setLineSpacing(AndroidUtilities.dp(2), 1.0f);
            timeText.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            addView(timeText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 30, 0, 0));

            if (verificationType == 3) {
                progressView = new ProgressView(context);
                addView(progressView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 3, 0, 12, 0, 0));
            }

            problemText = new TextView(context);
            problemText.setText(LocaleController.getString("DidNotGetTheCode", R.string.DidNotGetTheCode));
            problemText.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            problemText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            problemText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
            problemText.setLineSpacing(AndroidUtilities.dp(2), 1.0f);
            problemText.setPadding(0, AndroidUtilities.dp(2), 0, AndroidUtilities.dp(12));
            addView(problemText, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 0, 20, 0, 0));
            problemText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nextPressed) {
                        return;
                    }
                    if (nextType != 0 && nextType != 4) {
                        resendCode();
                    } else {
                        try {
                            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                            String version = String.format(Locale.US, "%s (%d)", pInfo.versionName, pInfo.versionCode);

                            Intent mailer = new Intent(Intent.ACTION_SEND);
                            mailer.setType("message/rfc822");
                            mailer.putExtra(Intent.EXTRA_EMAIL, new String[]{"sms@stel.com"});
                            mailer.putExtra(Intent.EXTRA_SUBJECT, "Android registration/login issue " + version + " " + phone);
                            mailer.putExtra(Intent.EXTRA_TEXT, "Phone: " + phone + "\nApp version: " + version + "\nOS version: SDK " + Build.VERSION.SDK_INT + "\nDevice Name: " + Build.MANUFACTURER + Build.MODEL + "\nLocale: " + Locale.getDefault() + "\nError: " + lastError);
                            getContext().startActivity(Intent.createChooser(mailer, "Send email..."));
                        } catch (Exception e) {
                            AlertsCreator.showSimpleAlert(PassportActivity.this, LocaleController.getString("NoMailInstalled", R.string.NoMailInstalled));
                        }
                    }
                }
            });
        }

        private void resendCode() {
            final Bundle params = new Bundle();
            params.putString("phone", phone);

            nextPressed = true;
            needShowProgress();

            final TLRPC.TL_auth_resendCode req = new TLRPC.TL_auth_resendCode();
            req.phone_number = phone;
            req.phone_code_hash = phoneHash;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                @Override
                public void run(final TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            nextPressed = false;
                            if (error == null) {
                                fillNextCodeParams(params, (TLRPC.TL_auth_sentCode) response, true);
                            } else {
                                AlertDialog dialog = (AlertDialog) AlertsCreator.processError(currentAccount, error, PassportActivity.this, req);
                                if (dialog != null && error.text.contains("PHONE_CODE_EXPIRED")) {
                                    dialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onBackPressed();
                                            finishFragment();
                                        }
                                    });
                                }
                            }
                            needHideProgress();
                        }
                    });
                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors);
        }

        @Override
        public void onCancelPressed() {
            nextPressed = false;
        }

        @Override
        public void setParams(Bundle params, boolean restore) {
            if (params == null) {
                return;
            }
            codeField.setText("");
            waitingForEvent = true;
            if (verificationType == 2) {
                AndroidUtilities.setWaitingForSms(true);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveSmsCode);
            } else if (verificationType == 3) {
                AndroidUtilities.setWaitingForCall(true);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didReceiveCall);
            }

            currentParams = params;
            phone = params.getString("phone");
            phoneHash = params.getString("phoneHash");
            timeout = time = params.getInt("timeout");
            openTime = (int) (System.currentTimeMillis() / 1000);
            nextType = params.getInt("nextType");
            pattern = params.getString("pattern");
            length = params.getInt("length");

            if (length != 0) {
                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = new InputFilter.LengthFilter(length);
                codeField.setFilters(inputFilters);
            } else {
                codeField.setFilters(new InputFilter[0]);
            }
            if (progressView != null) {
                progressView.setVisibility(nextType != 0 ? VISIBLE : GONE);
            }

            if (phone == null) {
                return;
            }

            String number = PhoneFormat.getInstance().format("+" + phone);
            CharSequence str = "";
            if (verificationType == 2) {
                str = AndroidUtilities.replaceTags(LocaleController.formatString("SentSmsCode", R.string.SentSmsCode, LocaleController.addNbsp(number)));
            } else if (verificationType == 3) {
                str = AndroidUtilities.replaceTags(LocaleController.formatString("SentCallCode", R.string.SentCallCode, LocaleController.addNbsp(number)));
            } else if (verificationType == 4) {
                str = AndroidUtilities.replaceTags(LocaleController.formatString("SentCallOnly", R.string.SentCallOnly, LocaleController.addNbsp(number)));
            }
            confirmTextView.setText(str);

            if (verificationType != 3) {
                AndroidUtilities.showKeyboard(codeField);
                codeField.requestFocus();
            } else {
                AndroidUtilities.hideKeyboard(codeField);
            }

            destroyTimer();
            destroyCodeTimer();

            lastCurrentTime = System.currentTimeMillis();
            if (verificationType == 3 && (nextType == 4 || nextType == 2)) {
                problemText.setVisibility(GONE);
                timeText.setVisibility(VISIBLE);
                if (nextType == 4) {
                    timeText.setText(LocaleController.formatString("CallText", R.string.CallText, 1, 0));
                } else if (nextType == 2) {
                    timeText.setText(LocaleController.formatString("SmsText", R.string.SmsText, 1, 0));
                }
                createTimer();
            } else if (verificationType == 2 && (nextType == 4 || nextType == 3)) {
                timeText.setVisibility(VISIBLE);
                timeText.setText(LocaleController.formatString("CallText", R.string.CallText, 2, 0));
                problemText.setVisibility(time < 1000 ? VISIBLE : GONE);
                createTimer();
            } else {
                timeText.setVisibility(GONE);
                problemText.setVisibility(GONE);
                createCodeTimer();
            }
        }

        private void createCodeTimer() {
            if (codeTimer != null) {
                return;
            }
            codeTime = 15000;
            codeTimer = new Timer();
            lastCodeTime = System.currentTimeMillis();
            codeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    double currentTime = System.currentTimeMillis();
                    double diff = currentTime - lastCodeTime;
                    codeTime -= diff;
                    lastCodeTime = currentTime;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (codeTime <= 1000) {
                                problemText.setVisibility(VISIBLE);
                                destroyCodeTimer();
                            }
                        }
                    });
                }
            }, 0, 1000);
        }

        private void destroyCodeTimer() {
            try {
                synchronized (timerSync) {
                    if (codeTimer != null) {
                        codeTimer.cancel();
                        codeTimer = null;
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        private void createTimer() {
            if (timeTimer != null) {
                return;
            }
            timeTimer = new Timer();
            timeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (timeTimer == null) {
                        return;
                    }
                    final double currentTime = System.currentTimeMillis();
                    double diff = currentTime - lastCurrentTime;
                    time -= diff;
                    lastCurrentTime = currentTime;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (time >= 1000) {
                                int minutes = time / 1000 / 60;
                                int seconds = time / 1000 - minutes * 60;
                                if (nextType == 4 || nextType == 3) {
                                    timeText.setText(LocaleController.formatString("CallText", R.string.CallText, minutes, seconds));
                                } else if (nextType == 2) {
                                    timeText.setText(LocaleController.formatString("SmsText", R.string.SmsText, minutes, seconds));
                                }
                                if (progressView != null) {
                                    progressView.setProgress(1.0f - (float) time / (float) timeout);
                                }
                            } else {
                                if (progressView != null) {
                                    progressView.setProgress(1.0f);
                                }
                                destroyTimer();
                                if (verificationType == 3) {
                                    AndroidUtilities.setWaitingForCall(false);
                                    NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
                                    waitingForEvent = false;
                                    destroyCodeTimer();
                                    resendCode();
                                } else if (verificationType == 2) {
                                    if (nextType == 4) {
                                        timeText.setText(LocaleController.getString("Calling", R.string.Calling));
                                        createCodeTimer();
                                        TLRPC.TL_auth_resendCode req = new TLRPC.TL_auth_resendCode();
                                        req.phone_number = phone;
                                        req.phone_code_hash = phoneHash;
                                        ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                                            @Override
                                            public void run(TLObject response, final TLRPC.TL_error error) {
                                                if (error != null && error.text != null) {
                                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            lastError = error.text;
                                                        }
                                                    });
                                                }
                                            }
                                        }, ConnectionsManager.RequestFlagFailOnServerErrors);
                                    } else if (nextType == 3) {
                                        AndroidUtilities.setWaitingForSms(false);
                                        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
                                        waitingForEvent = false;
                                        destroyCodeTimer();
                                        resendCode();
                                    }
                                }
                            }
                        }
                    });
                }
            }, 0, 1000);
        }

        private void destroyTimer() {
            try {
                synchronized (timerSync) {
                    if (timeTimer != null) {
                        timeTimer.cancel();
                        timeTimer = null;
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        @Override
        public void onNextPressed() {
            if (nextPressed) {
                return;
            }
            nextPressed = true;
            if (verificationType == 2) {
                AndroidUtilities.setWaitingForSms(false);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
            } else if (verificationType == 3) {
                AndroidUtilities.setWaitingForCall(false);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
            }
            waitingForEvent = false;
            final TLRPC.TL_account_verifyPhone req = new TLRPC.TL_account_verifyPhone();
            req.phone_number = phone;
            req.phone_code = codeField.getText().toString();
            req.phone_code_hash = phoneHash;
            destroyTimer();
            needShowProgress();
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                @Override
                public void run(final TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            needHideProgress();
                            nextPressed = false;
                            if (error == null) {
                                destroyTimer();
                                destroyCodeTimer();
                                delegate.saveValue(currentType, currentValues.get("phone"), null, null, null, null, null, null, null, new Runnable() {
                                    @Override
                                    public void run() {
                                        finishFragment();
                                    }
                                }, null);
                            } else {
                                lastError = error.text;
                                if (verificationType == 3 && (nextType == 4 || nextType == 2) || verificationType == 2 && (nextType == 4 || nextType == 3)) {
                                    createTimer();
                                }
                                if (verificationType == 2) {
                                    AndroidUtilities.setWaitingForSms(true);
                                    NotificationCenter.getGlobalInstance().addObserver(PhoneConfirmationView.this, NotificationCenter.didReceiveSmsCode);
                                } else if (verificationType == 3) {
                                    AndroidUtilities.setWaitingForCall(true);
                                    NotificationCenter.getGlobalInstance().addObserver(PhoneConfirmationView.this, NotificationCenter.didReceiveCall);
                                }
                                waitingForEvent = true;
                                if (verificationType != 3) {
                                    AlertsCreator.processError(currentAccount, error, PassportActivity.this, req);
                                }
                            }
                        }
                    });
                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors);
        }

        @Override
        public void onBackPressed() {
            TLRPC.TL_auth_cancelCode req = new TLRPC.TL_auth_cancelCode();
            req.phone_number = phone;
            req.phone_code_hash = phoneHash;
            ConnectionsManager.getInstance(currentAccount).sendRequest(req, new RequestDelegate() {
                @Override
                public void run(TLObject response, TLRPC.TL_error error) {

                }
            }, ConnectionsManager.RequestFlagFailOnServerErrors);

            destroyTimer();
            destroyCodeTimer();
            currentParams = null;
            if (verificationType == 2) {
                AndroidUtilities.setWaitingForSms(false);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
            } else if (verificationType == 3) {
                AndroidUtilities.setWaitingForCall(false);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
            }
            waitingForEvent = false;
        }

        @Override
        public void onDestroyActivity() {
            super.onDestroyActivity();
            if (verificationType == 2) {
                AndroidUtilities.setWaitingForSms(false);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveSmsCode);
            } else if (verificationType == 3) {
                AndroidUtilities.setWaitingForCall(false);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didReceiveCall);
            }
            waitingForEvent = false;
            destroyTimer();
            destroyCodeTimer();
        }

        @Override
        public void onShow() {
            super.onShow();
            if (codeField != null && codeField.getVisibility() == VISIBLE) {
                codeField.requestFocus();
                codeField.setSelection(codeField.length());
                AndroidUtilities.showKeyboard(codeField);
            }
        }

        @Override
        public void didReceivedNotification(int id, int account, Object... args) {
            if (!waitingForEvent || codeField == null) {
                return;
            }
            if (id == NotificationCenter.didReceiveSmsCode) {
                ignoreOnTextChange = true;
                codeField.setText("" + args[0]);
                ignoreOnTextChange = false;
                onNextPressed();
            } else if (id == NotificationCenter.didReceiveCall) {
                String num = "" + args[0];
                if (!AndroidUtilities.checkPhonePattern(pattern, num)) {
                    return;
                }
                ignoreOnTextChange = true;
                codeField.setText(num);
                ignoreOnTextChange = false;
                onNextPressed();
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();
        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        arrayList.add(new ThemeDescription(scrollView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        arrayList.add(new ThemeDescription(extraBackgroundView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));

        for (int a = 0; a < dividers.size(); a++) {
            arrayList.add(new ThemeDescription(dividers.get(a), ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_divider));
        }

        for (HashMap.Entry<SecureDocument, SecureDocumentCell> entry : documentsCells.entrySet()) {
            SecureDocumentCell cell = entry.getValue();
            arrayList.add(new ThemeDescription(cell, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{SecureDocumentCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
            arrayList.add(new ThemeDescription(cell, 0, new Class[]{SecureDocumentCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
            arrayList.add(new ThemeDescription(cell, 0, new Class[]{SecureDocumentCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        }

        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{TextDetailSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{TextSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{TextDetailSecureCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextDetailSecureCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextDetailSecureCell.class}, null, null, null, Theme.key_divider));
        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{TextDetailSecureCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{TextDetailSecureCell.class}, new String[]{"checkImageView"}, null, null, null, Theme.key_featuredStickers_addedIcon));

        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{HeaderCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(linearLayout2, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        arrayList.add(new ThemeDescription(linearLayout2, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));
        if (inputFields != null) {
            for (int a = 0; a < inputFields.length; a++) {
                arrayList.add(new ThemeDescription((View) inputFields[a].getParent(), ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite));
                arrayList.add(new ThemeDescription(inputFields[a], ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
                arrayList.add(new ThemeDescription(inputFields[a], ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
                arrayList.add(new ThemeDescription(inputFields[a], ThemeDescription.FLAG_HINTTEXTCOLOR | ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
                arrayList.add(new ThemeDescription(inputFields[a], ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField));
                arrayList.add(new ThemeDescription(inputFields[a], ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated));
                arrayList.add(new ThemeDescription(inputFields[a], ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_windowBackgroundWhiteRedText3));
            }
        } else {
            arrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
            arrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_HINTTEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteHintText));
            arrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_HINTTEXTCOLOR | ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
            arrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_windowBackgroundWhiteInputField));
            arrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_windowBackgroundWhiteInputFieldActivated));
            arrayList.add(new ThemeDescription(null, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_windowBackgroundWhiteRedText3));
        }

        arrayList.add(new ThemeDescription(emptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle));
        arrayList.add(new ThemeDescription(noPasswordImageView, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chat_messagePanelIcons));
        arrayList.add(new ThemeDescription(noPasswordTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));
        arrayList.add(new ThemeDescription(noPasswordSetTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlueText5));
        arrayList.add(new ThemeDescription(passwordForgotButton, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlueText4));

        arrayList.add(new ThemeDescription(plusTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlackText));

        arrayList.add(new ThemeDescription(acceptTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_passport_authorizeText));
        arrayList.add(new ThemeDescription(bottomLayout, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_passport_authorizeBackground));
        arrayList.add(new ThemeDescription(bottomLayout, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_passport_authorizeBackgroundSelected));

        arrayList.add(new ThemeDescription(progressView, 0, null, null, null, null, Theme.key_contextProgressInner2));
        arrayList.add(new ThemeDescription(progressView, 0, null, null, null, null, Theme.key_contextProgressOuter2));
        arrayList.add(new ThemeDescription(progressViewButton, 0, null, null, null, null, Theme.key_contextProgressInner2));
        arrayList.add(new ThemeDescription(progressViewButton, 0, null, null, null, null, Theme.key_contextProgressOuter2));

        arrayList.add(new ThemeDescription(emptyImageView, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_sessions_devicesImage));
        arrayList.add(new ThemeDescription(emptyTextView1, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(emptyTextView2, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(emptyTextView3, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_windowBackgroundWhiteBlueText4));

        return arrayList.toArray(new ThemeDescription[arrayList.size()]);
    }
}
