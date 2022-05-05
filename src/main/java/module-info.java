module com.jp2p.jp2p {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires json.simple;

    opens com.jp2p.gui to javafx.fxml;
    exports com.jp2p.gui;
}