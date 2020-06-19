module com.atomiccomics.crusoe {
    requires java.logging;
    requires javafx.controls;
    requires javafx.media;
    requires io.reactivex.rxjava3;

    opens com.atomiccomics.crusoe to javafx.graphics;
}