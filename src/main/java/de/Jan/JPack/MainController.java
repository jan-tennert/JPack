package de.Jan.JPack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML
    public ChoiceBox<Type> type;
    @FXML
    public Button java_chooser;
    @FXML
    public Button jar_chooser;
    @FXML
    public TextField jar_path;
    @FXML
    public TextField java_dir;
    @FXML
    public TextField main_class;
    @FXML
    public TextField app_name;
    @FXML
    public TextField app_desc;
    @FXML
    public TextField app_v;
    @FXML
    public Button icon_button;
    @FXML
    public TextField app_copy;
    @FXML
    public TextField icon_path;
    @FXML
    public Button output_button;
    @FXML
    public TextField output_dir;
    @FXML
    public CheckBox create_shortcut;
    @FXML
    public CheckBox dir_chooser;
    @FXML
    public CheckBox system_menu;
    @FXML
    public TextField system_group;
    @FXML
    public CheckBox user_mode;

    private File jar_File;
    private File output;
    private File jpackage;
    private File jar_Input;
    private File app_icon;

    public void initialize() {
        type.getItems().addAll( new Type("EXE (Windows)", "exe"), new Type("MSI (Windows)", "msi"));
    }

    public void onJarChooser(ActionEvent e) {
        FileChooser f = new FileChooser();
        f.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAR File", "*.jar"));
        f.setTitle("Select the JAR File");

        jar_File = f.showOpenDialog(App.s);
        if(jar_File != null) {
            jar_path.setText(jar_File.getAbsolutePath());
            jar_Input = jar_File.getParentFile();
        } else {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("No file selected.");
            a.showAndWait();
            jar_File = null;
        }
    }

    public void onJDKDir(ActionEvent e) {
        DirectoryChooser d = new DirectoryChooser();
        d.setTitle("Select the jdk folder");
        String path = d.showDialog(App.s).getAbsolutePath();
        String jpackage_path = path + "/bin/jpackage.exe";
        jpackage = new File(jpackage_path);
        if(!jpackage.exists()) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("The selected folder is not a jdk 14+ folder.");
            a.showAndWait();
            jpackage = null;
        } else {
            java_dir.setText(path);
        }
    }

    public void onIconChange(ActionEvent e) {
        FileChooser f = new FileChooser();
        f.setTitle("Select the icon");
        f.getExtensionFilters().add(new FileChooser.ExtensionFilter("Icon", "*.ico"));
        app_icon = f.showOpenDialog(App.s);
        if(app_icon == null) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("No file selected.");
            a.showAndWait();
        } else {
            icon_path.setText(app_icon.getAbsolutePath());
        }
    }

    public void onOutputDir(ActionEvent e) {
        DirectoryChooser d = new DirectoryChooser();
        d.setTitle("Select the output folder");
        output = d.showDialog(App.s);
        if(output == null) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("No folder selected.");
            a.showAndWait();
        } else {
            output_dir.setText(output.getAbsolutePath());
        }
    }

    public void onCompile(ActionEvent e) {
        if(jpackage == null || jar_File == null || main_class.getText().equals("") || type == null || output == null) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("A required field is missing.");
            a.showAndWait();
        } else {
            String jp = "\"" + jpackage.getAbsolutePath() + "\"";
            String in = "\"" + jar_Input.getAbsolutePath() + "\"";
            String out = "\"" + output.getAbsolutePath() + "\"";
            String jar = "\"" + jar_File.getName() + "\"";
            String main = main_class.getText();
            String type = this.type.getSelectionModel().getSelectedItem().getShortcut();

            String command = jp + " --type " + type + " --dest " + out + " --input " + in + " --main-jar " + jar + " --main-class " + main;
            if(app_icon != null) {
                command += " --icon \"" + app_icon.getAbsolutePath() + "\"";
            } if(!app_desc.getText().equals("")) {
                command += " --description \"" + app_desc.getText() + "\"";
            } if(!app_name.getText().equals("")) {
                command += " --name \"" + app_name.getText() + "\"";
            } if(!app_v.getText().equals("")) {
                command += " --app-version \"" + app_v.getText() + "\"";
            } if(!app_copy.getText().equals("")) {
                command += " --copyright \"" + app_copy.getText() + "\"";
            }
            if(create_shortcut.selectedProperty().get()) {
                command += " --win-shortcut";
            }
            if(dir_chooser.selectedProperty().get()) {
                command += " --win-dir-chooser";
            }
            if(system_menu.selectedProperty().get()) {
                command += " --win-menu";
                if(!system_group.getText().equals("")) {
                    command += " --win-menu-group \"" + system_group.getText() + "\"";
                }
            }
            System.out.println(command);
            try {
                Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"" + command + "\"");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    public class Type {

        private String name;
        private String s;

        public Type(String name, String s) {
            this.name = name;
            this.s = s;
        }

        public String getName() {
            return name;
        }

        public String getShortcut() {
            return s;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
