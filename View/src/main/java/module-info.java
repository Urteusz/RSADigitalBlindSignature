module pl.kryptografia.view {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    requires pl.kryptografia.model;

    opens pl.kryptografia.view to javafx.fxml;
    exports pl.kryptografia.view;
}