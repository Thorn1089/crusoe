module com.atomiccomics.crusoe {
    requires java.logging;
    requires javafx.controls;

    opens com.atomiccomics.crusoe to javafx.graphics;
}