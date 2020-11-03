package de.Jan.JPack;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
    @FXML
    public ChoiceBox<Assocation> assocations;
    @FXML
    public TextField description;
    @FXML
    public TextField extension;
    @FXML
    public TextField mime;
    @FXML
    public TextField icon_as;

    private File jar_File;
    private File output;
    private File jpackage;
    private File jar_Input;
    private File app_icon;
    private Gson gson = new Gson();
    private Type standard =  new Type("EXE (Windows)", "exe");
    private Type msi = new Type("MSI (Windows)", "msi");
    public static File currentSettingsFile = null;
    private File ass_icon;
    public static File ass;


    public void initialize() throws FileNotFoundException {
        File folder = new File(System.getProperty("java.io.tmpdir") + "/JPack");
        if(folder != null && folder.listFiles() != null && folder.listFiles().length != 0) {
            for(File f : folder.listFiles()) {
                if(!f.isDirectory() && f.getName().contains(".properties")) {
                    f.delete();
                }
            }
        }

        if(ass != null) {
            File file = ass;
            if(file.exists()) {
               openSettingsFile(file);
            }
        }

        type.getItems().addAll(standard, msi);
        type.getSelectionModel().select(standard);
        openFile.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_ANY));
        saveFile.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_ANY));
        saveas.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_ANY));
        close.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_ANY));
        compile.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY));
        newFile.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_ANY));
        assocations.getSelectionModel().selectedItemProperty().addListener(l -> {
            Assocation as = assocations.getSelectionModel().getSelectedItem();
            if(as == null) return;
            extension.setText(as.getExtension());
            mime.setText(as.getMimiType());
            icon_as.setText(as.getIconPath());
            ass_icon = new File(as.getIconPath());
            description.setText(as.getDescription());
        });
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
        File javahomeDir = (javaHome != null) ? new File(javaHome): null;
        DirectoryChooser d = new DirectoryChooser();
        if(javahomeDir != null && javahomeDir.exists()) {
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

    public void onCompile(ActionEvent e) throws IOException, InterruptedException {
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

            StringBuilder command = new StringBuilder(jp + " --type " + type + " --dest " + out + " --input " + in + " --main-jar " + jar + " --main-class " + main);
            if(app_icon != null) {
                command.append(" --icon \"").append(app_icon.getAbsolutePath()).append("\"");
            } if(!app_desc.getText().equals("")) {
                command.append(" --description \"").append(app_desc.getText()).append("\"");
            } if(!app_name.getText().equals("")) {
                command.append(" --name \"").append(app_name.getText()).append("\"");
            } if(!app_v.getText().equals("")) {
                command.append(" --app-version \"").append(app_v.getText()).append("\"");
            } if(!app_copy.getText().equals("")) {
                command.append(" --copyright \"").append(app_copy.getText()).append("\"");
            }
            if(create_shortcut.selectedProperty().get()) {
                command.append(" --win-shortcut");
            }
            if(dir_chooser.selectedProperty().get()) {
                command.append(" --win-dir-chooser");
            }
            if(system_menu.selectedProperty().get()) {
                command.append(" --win-menu");
                if(!system_group.getText().equals("")) {
                    command.append(" --win-menu-group \"").append(system_group.getText()).append("\"");
                }
            }
            if(!vendor.equals("")) {
                command.append(" --vendor \"").append(vendor).append("\"");
            }
            if(console_mode.selectedProperty().get()) {
                command.append(" --win-console");
            }
            if(!java_options.getText().equals("")) {
                command.append(" --java-options \"").append(java_options.getText()).append("\"");
            }
            if(!module_path.getText().equals("") && !add_modules.getText().equals("")) {
                command.append(" --module-path \"").append(module_path.getText()).append("\" --add-modules ").append(add_modules.getText());
            }
            if(!user_mode.getText().equals("")) {
                command.append(" --win-per-user-install");
            }
            for(Assocation a : assocations.getItems()) {
                command.append(" --file-associations \"").append(a.getFile().getAbsolutePath()).append("\"");
            }
            System.out.println("[COMMAND] " + command);
            Process p = Runtime.getRuntime().exec(command.toString());

           Platform.runLater(() -> {
               new Thread(() -> {
                   try {
                       p.waitFor();

                       BufferedReader stdInput = new BufferedReader(new
                               InputStreamReader(p.getInputStream()));

                       BufferedReader stdError = new BufferedReader(new
                               InputStreamReader(p.getErrorStream()));

                       String s = null;
                       while ((s = stdInput.readLine()) != null) {
                           System.out.println(s);
                       }
                       while ((s = stdError.readLine()) != null) {
                           System.out.println(s);
                       }

                       stdError.close();
                       stdInput.close();
                   } catch(IOException | InterruptedException er) {
                       er.printStackTrace();
                   }
               }).start();
            });
            try {
                if(p.waitFor() == 0) {
                    System.out.println("test");
                }
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            System.out.println("a");
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
           openSettingsFile(file);
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

    private void openSettingsFile(File file) throws FileNotFoundException {
        JsonReader j = new JsonReader(new FileReader(file));
        Settings s = gson.fromJson(j, Settings.class);
        type.getSelectionModel().select((s.type.equals("exe")) ? standard : msi);
        jar_File = !s.jarFilePath.equals("") ? new File(s.jarFilePath) : null;
        jar_path.setText(s.jarFilePath);
        jar_Input = jar_File.getParentFile();
        main_class.setText(s.mainClass);
        java_dir.setText(s.JDKDir);
        output_dir.setText(s.outputPath);
        output = !s.outputPath.equals("") ? new File(s.outputPath) : null;
        app_name.setText(s.name);
        app_desc.setText(s.description);
        app_copy.setText(s.copyright);
        app_v.setText(s.version);
        icon_path.setText(s.iconPath);
        app_icon = !s.iconPath.equals("") ? new File(s.iconPath) : null;
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
        currentSettingsFile = file;
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

    public void onFileIcon(ActionEvent e) {
        FileChooser f = new FileChooser();
        f.getExtensionFilters().add(new FileChooser.ExtensionFilter("ICO", "*.ico"));
        f.setTitle("Select the ico file");
        File file = f.showOpenDialog(App.s);
        if(file == null) return;
        ass_icon = file;
        icon_as.setText(file.getAbsolutePath());
    }

    public void add_button(ActionEvent e) throws IOException {
        String extension = this.extension.getText();
        String description = this.description.getText();
        String mime = this.mime.getText();
        String iconPath = this.icon_as.getText();

        if(extension.equals("") || description.equals("") || mime.equals("") || iconPath.equals("")) {
            Alert a = new Alert(AlertType.ERROR);
            a.setHeaderText(null);
            a.setTitle("Error");
            a.setContentText("A required field is missing");
            a.show();
            return;
        }
        for(Assocation a : assocations.getItems()) {
            if(a.getExtension().equals(extension)) {
                Alert a2 = new Alert(AlertType.ERROR);
                a2.setHeaderText(null);
                a2.setTitle("Error");
                a2.setContentText("A assocation with this extension already exsists!");
                a2.show();
                return;
            }
        }
        File folder = new File(System.getProperty("java.io.tmpdir") + "/JPack");
        folder.mkdirs();
        File f = new File(folder.getAbsolutePath() + "/" + extension + ".properties");
        if(!f.createNewFile()) {
            Alert a2 = new Alert(AlertType.ERROR);
            a2.setHeaderText(null);
            a2.setTitle("Error");
            a2.setContentText("A assocation with this extension already exsists!");
            a2.show();
            return;
        }
        Properties p = new Properties();
        p.load(new FileInputStream(f));
        p.setProperty("extension", extension);
        p.setProperty("mime-type", mime);
        p.setProperty("icon", iconPath);
        p.setProperty("description", description);

        try (FileWriter fileWriter = new FileWriter(f)) {
          p.store(fileWriter, null);
          fileWriter.flush();
        }
        assocations.getItems().add(new Assocation(extension, mime, iconPath, description, f));
    }

    public void onSaveFile(ActionEvent e) throws IOException {
        Assocation a = assocations.getSelectionModel().getSelectedItem();
        if(a == null) {
            return;
        }
        String extension = this.extension.getText();
        String description = this.description.getText();
        String mime = this.mime.getText();
        String iconPath = this.icon_as.getText();

        if(extension.equals("") || description.equals("") || mime.equals("") || iconPath.equals("")) {
            Alert a2 = new Alert(AlertType.ERROR);
            a2.setHeaderText(null);
            a2.setTitle("Error");
            a2.setContentText("A required field is missing");
            a2.show();
            return;
        }
        for(Assocation a2 : assocations.getItems()) {
            if(!a2.getExtension().equals(a.getExtension()) && a2.getExtension().equals(extension)) {
                Alert a3 = new Alert(AlertType.ERROR);
                a3.setHeaderText(null);
                a3.setTitle("Error");
                a3.setContentText("A assocation with this extension already exists!");
                a3.show();
                return;
            }
        }
        a.setDescription(description);
        a.setExtension(extension);
        a.setMimiType(mime);
        a.setIconPath(iconPath);

        Properties p = new Properties();
        p.load(new FileInputStream(a.getFile()));
        p.setProperty("extension", extension);
        p.setProperty("mime-type", mime);
        p.setProperty("icon", iconPath);
        p.setProperty("description", description);

        try (FileWriter fileWriter = new FileWriter(a.getFile())) {
            p.store(fileWriter, null);
            fileWriter.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void onDelete(ActionEvent e) {
        if(assocations.getSelectionModel().getSelectedItem() != null) {
            Assocation a = assocations.getSelectionModel().getSelectedItem();
            a.getFile().deleteOnExit();
            assocations.getItems().remove(a);
        }
    }

    public Assocation getAssociationFromFile(File f) throws IOException {
        Properties properties = new Properties();
        FileInputStream in = new FileInputStream(f);
        properties.load(in);
        Assocation a = new Assocation(properties.getProperty("extension"), properties.getProperty("mime-type"), properties.getProperty("icon"), properties.getProperty("description"), f);
        in.close();
        return a;
    }

    public void exportZip(ActionEvent e) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));
        chooser.setTitle("Choose the save location");
        File file = chooser.showSaveDialog(App.s);
        if(file != null) {
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
            for (int i = 0; i < assocations.getItems().size(); i++) {
                FileInputStream input = new FileInputStream(assocations.getItems().get(i).getFile());
                zip.putNextEntry(new ZipEntry(assocations.getItems().get(i).getExtension() + ".jsettings"));

                byte[] b = new byte[1024];
                int count;

                while ((count = input.read(b)) > 0) {
                    zip.write(b, 0, count);
                }
                input.close();
            }
            zip.setComment("File Associations for JPack");
            zip.close();
            Alert a = new Alert(AlertType.CONFIRMATION);
            a.setHeaderText(null);
            a.setTitle("Information");
            a.setContentText("The zip archive was created!");

            ButtonType openFile = new ButtonType("Open File");
            ButtonType openFolder = new ButtonType("Open Folder");
            ButtonType cancel = new ButtonType("Cancel");
            a.getButtonTypes().setAll(openFile, openFolder, cancel);

            Optional<ButtonType> result = a.showAndWait();
            if(result.get().equals(openFile)) {
                Desktop.getDesktop().open(file);
            } else if(result.get().equals(openFolder)) {
                Desktop.getDesktop().open(file.getParentFile());
            }
        }
    }

    public void importZip(ActionEvent e) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));
        chooser.setTitle("Select the zip archive");
        File f = chooser.showOpenDialog(App.s);
        if(f != null) {
            File folder = new File(System.getProperty("java.io.tmpdir") + "/JPack");
            List<File> files = unzipFiles(f.getAbsolutePath(), folder);
            for(File file : files) {
                assocations.getItems().add(getAssociationFromFile(file));
            }
        }
    }

    public List<File> unzipFiles(String zip, File destination) throws IOException {
        List<File> files = new ArrayList<>();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destination, zipEntry);
            files.add(newFile);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return files;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private final class Type {

        private String name;

        private String s;

        public Type(String name, String s) {
            this.name = name;
            this.s = s;
        }

        public String getName() {
            return name;
        }

        public String getS() {
            return s;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public final class Settings {

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

    public final class Assocation {

        private String extension;
        private String mimiType;
        private String iconPath;
        private String description;
        private File file;

        public Assocation(String extension, String mimi_type, String iconPath, String description, File file) {
            this.extension = extension;
            this.mimiType = mimi_type;
            this.iconPath = iconPath;
            this.description = description;
            this.file = file;
        }

        public String getExtension() {
            return extension;
        }

        public String getMimiType() {
            return mimiType;
        }

        public String getIconPath() {
            return iconPath;
        }

        public String getDescription() {
            return description;
        }

        public File getFile() {
            return file;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public void setMimiType(String mimiType) {
            this.mimiType = mimiType;
        }

        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return getExtension();
        }

    }

}
