package de.Jan.JPack;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.io.*;

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
    @FXML
    public TextField vendor;
    @FXML
    public CheckBox console_mode;
    @FXML
    public TextArea java_options;
    @FXML
    public TextArea module_path;
    @FXML
    public TextArea add_modules;
    @FXML
    public MenuItem openFile;
    @FXML
    public MenuItem saveFile;
    @FXML
    public MenuItem saveas;
    @FXML
    public MenuItem close;
    @FXML
    public MenuItem compile;
    @FXML
    public MenuItem newFile;

    private File jar_File;
    private File output;
    private File jpackage;
    private File jar_Input;
    private File app_icon;
    private Gson gson = new Gson();
    private Type standard =  new Type("EXE (Windows)", "exe");
    private Type msi = new Type("MSI (Windows)", "msi");
    private File currentSettingsFile = null;


    public void initialize() {
        type.getItems().addAll(standard, msi);
        type.getSelectionModel().select(standard);
        openFile.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_ANY));
        saveFile.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY));
        saveas.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_ANY));
        close.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_ANY));
        compile.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY));
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_ANY));
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
        String javaHome = System.getenv("JAVA_HOME");
        File javahomeDir = new File(javaHome);
        DirectoryChooser d = new DirectoryChooser();
        if(javahomeDir.exists()) {
            d.setInitialDirectory(javahomeDir);
        }
        d.setTitle("Select the jdk folder");
        File f = d.showDialog(App.s);
        if(f == null) return;
        String path = f.getAbsolutePath();
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
            String vendor = this.vendor.getText();
            String type = this.type.getSelectionModel().getSelectedItem().getS();

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
            if(!vendor.equals("")) {
                command += " --vendor \"" + vendor + "\"";
            }
            if(console_mode.selectedProperty().get()) {
                command += " --win-console";
            }
            if(!java_options.getText().equals("")) {
                command += " --java-options \"" + java_options.getText() + "\"";
            }
            if(!module_path.getText().equals("") && !add_modules.getText().equals("")) {
                command += " --module-path \"" + module_path.getText() + "\" --add-modules " + add_modules.getText();
            }
            if(!user_mode.getText().equals("")) {
                command += " --win-per-user-install";
            }
            try {
                System.out.println(command);
                Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"" + command + "\"");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    public void onSave(ActionEvent e) {
        FileChooser f = new FileChooser();
        f.setTitle("Save the file");
        f.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSettings File", "*.jsettings"));
        File file = f.showSaveDialog(App.s);
        if(file == null) return;
        Settings s = new Settings(type.getSelectionModel().getSelectedItem().s,
                !(jar_File == null) ? jar_File.getAbsolutePath() : "",
                main_class.getText(),
                java_dir.getText(), output_dir.getText(),
                app_name.getText(),
                app_desc.getText(),
                app_copy.getText(),
                app_v.getText(),
                icon_path.getText(),
                vendor.getText(),
                create_shortcut.isSelected(),
                dir_chooser.isSelected(),
                system_menu.isSelected(),
                system_group.getText(),
                user_mode.isSelected(),
                console_mode.isSelected(),
                java_options.getText(),
                module_path.getText(),
                add_modules.getText());
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(s));
            writer.flush();
            writer.close();
            currentSettingsFile = file;
            Alert a = new Alert(AlertType.INFORMATION);
            a.setContentText("The settings file was created!");
            a.setTitle("Information");
            a.setHeaderText(null);
            a.show();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void onOpen(ActionEvent e) throws FileNotFoundException {
        FileChooser f = new FileChooser();
        f.setTitle("Choose the jsettings file.");
        f.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSettings File", "*.jsettings"));
        File file = f.showOpenDialog(App.s);
        if(file != null && file.exists()) {
            JsonReader j = new JsonReader(new FileReader(file));
            Settings s = gson.fromJson(j, Settings.class);
            type.getSelectionModel().select((s.type.equals("exe")) ? standard : msi);
            jar_File = new File(s.jarFilePath);
            jar_path.setText(s.jarFilePath);
            jar_Input = jar_File.getParentFile();
            main_class.setText(s.mainClass);
            java_dir.setText(s.JDKDir);
            output_dir.setText(s.outputPath);
            output = new File(s.outputPath);
            app_name.setText(s.name);
            app_desc.setText(s.description);
            app_copy.setText(s.copyright);
            app_v.setText(s.version);
            icon_path.setText(s.iconPath);
            app_icon = new File(s.iconPath);
            vendor.setText(s.publisher);
            create_shortcut.setSelected(s.desktopShortcut);
            dir_chooser.setSelected(s.userInstallFolder);
            system_menu.setSelected(s.createSystemMenuItem);
            system_group.setText(s.systemMenuName);
            user_mode.setSelected(s.installWithUserRights);
            console_mode.setSelected(s.consoleApplication);
            java_options.setText(s.javaOption);
            module_path.setText(s.modulePath);
            add_modules.setText(s.modules);
            jpackage = new File(java_dir.getText() + "/bin/jpackage.exe");
        }
    }

    public void currentSave(ActionEvent e) throws IOException {
        if(currentSettingsFile == null || !currentSettingsFile.exists()) {
            onSave(null);
        } else {
            Settings s = new Settings(type.getSelectionModel().getSelectedItem().s,
                    !(jar_File == null) ? jar_File.getAbsolutePath() : "",
                    main_class.getText(),
                    java_dir.getText(), output_dir.getText(),
                    app_name.getText(),
                    app_desc.getText(),
                    app_copy.getText(),
                    app_v.getText(),
                    icon_path.getText(),
                    vendor.getText(),
                    create_shortcut.isSelected(),
                    dir_chooser.isSelected(),
                    system_menu.isSelected(),
                    system_group.getText(),
                    user_mode.isSelected(),
                    console_mode.isSelected(),
                    java_options.getText(),
                    module_path.getText(),
                    add_modules.getText());
            FileWriter writer = new FileWriter(currentSettingsFile);
            writer.write(gson.toJson(s));
            writer.flush();
            writer.close();

        }
    }

    public void onClose(ActionEvent e) {
        System.exit(0);
    }

    public void onNew(ActionEvent e) {
        jar_path.setText("");
        java_dir.setText("");
        main_class.setText("");
        app_name.setText("");
        app_v.setText("");
        app_desc.setText("");
        app_copy.setText("");
        app_icon = null;
        vendor.setText("");
        icon_path.setText("");
        output_dir.setText("");
        output = null;
        jar_File = null;
        jpackage = null;
        create_shortcut.setSelected(false);
        user_mode.setSelected(false);
        system_menu.setSelected(false);
        system_group.setText("");
        add_modules.setText("");
        module_path.setText("");
        console_mode.setSelected(false);
        java_options.setText("");
        dir_chooser.setSelected(false);
    }

    private class Type {

        @Getter
        private String name;
        @Getter
        private String s;

        public Type(String name, String s) {
            this.name = name;
            this.s = s;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public class Settings {

        private String type;
        private String jarFilePath;
        private String mainClass;
        private String JDKDir;
        private String outputPath;
        private String name;
        private String description;
        private String copyright;
        private String version;
        private String iconPath;
        private String publisher;
        private boolean desktopShortcut;
        private boolean userInstallFolder;
        private boolean createSystemMenuItem;
        private String systemMenuName;
        private boolean installWithUserRights;
        private boolean consoleApplication;
        private String javaOption;
        private String modulePath;
        private String modules;

        public Settings(String type, String jarFilePath, String mainClass, String JDKDir, String outputPath, String name, String description, String copyright, String version, String iconPath, String publisher, boolean desktopShortcut, boolean userInstallFolder, boolean createSystemMenuItem, String systemMenuName, boolean installWithUserRights, boolean consoleApplication, String javaOption, String modulePath, String modules) {
            this.type = type;
            this.jarFilePath = jarFilePath;
            this.mainClass = mainClass;
            this.JDKDir = JDKDir;
            this.outputPath = outputPath;
            this.name = name;
            this.description = description;
            this.copyright = copyright;
            this.version = version;
            this.iconPath = iconPath;
            this.publisher = publisher;
            this.desktopShortcut = desktopShortcut;
            this.userInstallFolder = userInstallFolder;
            this.createSystemMenuItem = createSystemMenuItem;
            this.systemMenuName = systemMenuName;
            this.installWithUserRights = installWithUserRights;
            this.consoleApplication = consoleApplication;
            this.javaOption = javaOption;
            this.modulePath = modulePath;
            this.modules = modules;
        }

    }

}
