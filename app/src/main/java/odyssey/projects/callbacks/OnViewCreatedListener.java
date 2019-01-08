package odyssey.projects.callbacks;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public interface OnViewCreatedListener {
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState);
}
