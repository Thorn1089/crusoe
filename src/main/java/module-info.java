module com.atomiccomics.crusoe {
    requires java.logging;
    requires javafx.controls;
    requires javafx.media;
    requires javafx.fxml;
    requires io.reactivex.rxjava3;
    requires io.github.classgraph;
    requires com.google.guice;

    opens com.atomiccomics.crusoe to javafx.graphics, javafx.fxml;
    exports com.atomiccomics.crusoe to com.google.guice;
    exports com.atomiccomics.crusoe.player to com.google.guice;
    exports com.atomiccomics.crusoe.world to com.google.guice;
    exports com.atomiccomics.crusoe.time to com.google.guice;
    exports com.atomiccomics.crusoe.ui to com.google.guice;
}