package odyssey.projects.callbacks;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnViewCreatedListener {
    void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState);
}
