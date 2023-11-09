package ru.yojo.codegen;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import ru.yojo.codegen.domain.LombokProperties;
import ru.yojo.codegen.generator.YojoGenerator;
import ru.yojo.codegen.mapper.MessageMapper;
import ru.yojo.codegen.mapper.SchemaMapper;

import java.io.File;

import static ru.yojo.codegen.constants.ConstantsEnum.*;

/**
 * Desktop version of POJO generator
 * <p>
 * Developed by: Vladimir Morozkin
 * March 2023
 */

@Component
public class YojoDesktopGUI implements ApplicationListener<YojoApplication.StageReadyEvent> {

    private final SchemaMapper schemaMapper;
    private final MessageMapper messageMapper;

    public YojoDesktopGUI(SchemaMapper schemaMapper, MessageMapper messageMapper) {
        this.schemaMapper = schemaMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public void onApplicationEvent(YojoApplication.StageReadyEvent stageReadyEvent) {
        Stage stage = stageReadyEvent.getStage();
        final FileChooser fileChooser = new FileChooser();
        final DirectoryChooser directoryChooser = new DirectoryChooser();

        configuringDirectoryChooser(directoryChooser);
        configuringFileChooser(fileChooser);

        TextField directoryTextArea = new TextField();
        directoryTextArea.promptTextProperty().set("Input directory");
        directoryTextArea.setMinHeight(30);
        directoryTextArea.setMaxHeight(30);

        TextField fileTextArea = new TextField();
        fileTextArea.promptTextProperty().set("Choose file");
        fileTextArea.setMinHeight(30);
        fileTextArea.setMaxHeight(30);

        TextField packageTextArea = new TextField();
        packageTextArea.promptTextProperty().set("Input package to generate");
        fileTextArea.setMinHeight(30);
        fileTextArea.setMaxHeight(30);

        Button selectFileButton = new Button("Select Yaml File");
        Button selectDirectoryButton = new Button("Select Result Directory");
        Button generateClassesButton = new Button("Generate Java Classes");
        generateClassesButton.setMinHeight(50);
        generateClassesButton.setMinWidth(100);

        CheckBox lombok = new CheckBox("Lombok Annotations");
        lombok.setFont(new Font("System Bold", 12));
        lombok.setSelected(false);
        lombok.setVisible(true);
        CheckBox allArgsConstructor = new CheckBox(LOMBOK_ALL_ARGS_CONSTRUCTOR_ANNOTATION.getValue());
        allArgsConstructor.setFont(new Font("System Bold", 12));
        CheckBox accessors = new CheckBox(LOMBOK_ACCESSORS_EMPTY_ANNOTATION.getValue());
        CheckBox accessorFluent = new CheckBox(LOMBOK_ACCESSORS_FLUENT_ANNOTATION.getValue());
        CheckBox accessorChain = new CheckBox(LOMBOK_ACCESSORS_CHAIN_ANNOTATION.getValue());
        accessors.setFont(new Font("System Bold", 12));
        accessorFluent.setFont(new Font("System Bold", 12));
        accessorChain.setFont(new Font("System Bold", 12));

        allArgsConstructor.setVisible(false);
        accessors.setVisible(false);
        accessorFluent.setVisible(false);
        accessorChain.setVisible(false);

        lombok.setOnAction(event -> {
            if (lombok.isSelected()) {
                allArgsConstructor.setVisible(true);
                accessors.setVisible(true);
            } else {
                allArgsConstructor.setVisible(false);
                allArgsConstructor.setSelected(false);
                accessors.setVisible(false);
                accessors.setSelected(false);
                accessorFluent.setVisible(false);
                accessorFluent.setSelected(false);
                accessorChain.setVisible(false);
                accessorChain.setSelected(false);
            }
        });

        accessors.setOnAction(event -> {
            if (accessors.isSelected()) {
                accessorFluent.setVisible(true);
                accessorChain.setVisible(true);
            }
        });

        selectDirectoryButton.setOnAction(event -> {
            File dir = directoryChooser.showDialog(stage);
            if (dir != null) {
                directoryTextArea.setText(dir.getAbsolutePath());
            } else {
                directoryTextArea.setText(null);
            }
        });

        selectFileButton.setOnAction(event -> {
            fileTextArea.clear();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                fileTextArea.setText(file.getAbsolutePath());
            } else {
                fileTextArea.setText(null);
            }
        });

        generateClassesButton.setOnAction(event -> {
            String directoryPath = directoryTextArea.getText();
            String filePath = fileTextArea.getText();
            String packagePath = packageTextArea.getText();
            boolean enableLombok = lombok.selectedProperty().get();
            boolean addAllArgs = allArgsConstructor.selectedProperty().get();
            boolean addAccessors = accessors.selectedProperty().get();
            boolean addFluent = accessorFluent.selectedProperty().get();
            boolean addChain = accessorChain.selectedProperty().get();
            if (StringUtils.isNotBlank(directoryPath) && StringUtils.isNotBlank(filePath)) {
                YojoGenerator yamlToPojo = new YojoGenerator(schemaMapper, messageMapper);
                yamlToPojo.generate(
                        filePath.replace("\\", "/"),
                        directoryPath.replace("\\", "/"),
                        packagePath,
                        new LombokProperties(enableLombok, addAllArgs,
                                new LombokProperties.Accessors(addAccessors, addFluent, addChain)));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("YOJO");
                alert.setHeaderText("Java classes is generated!");
                alert.setContentText("Done!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("YOJO");
                alert.setHeaderText("Failed to generate Java classes");
                alert.setContentText("Specify the correct path to the file/directory!");
                alert.showAndWait();
            }
        });

        HBox lombokBox = new HBox();
        lombokBox.setSpacing(8);
        lombokBox.setPadding(new Insets(5));
        lombokBox.getChildren().add(lombok);
        lombokBox.getChildren().add(allArgsConstructor);
        lombokBox.getChildren().add(accessors);

        HBox accessorsBox = new HBox();
        accessorsBox.setSpacing(8);
        accessorsBox.setPadding(new Insets(5));
        lombokBox.getChildren().add(accessorChain);
        lombokBox.getChildren().add(accessorFluent);

        HBox generateButtonBox = new HBox();
        generateButtonBox.setSpacing(8);
        generateButtonBox.setPadding(new Insets(20, 0, 0, 0));
        generateButtonBox.getChildren().add(generateClassesButton);

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(8);

        root.getChildren().addAll(fileTextArea, selectFileButton);
        root.getChildren().addAll(directoryTextArea, selectDirectoryButton);
        root.getChildren().add(packageTextArea);
        root.getChildren().add(lombokBox);
        root.getChildren().add(generateButtonBox);

        root.setId("pane");

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().addAll("css/style.css");
        stage.setTitle("YOJO");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image("css/icon.png"));
        stage.show();
    }

    private void configuringDirectoryChooser(DirectoryChooser directoryChooser) {
        // Set title for FileChooser
        directoryChooser.setTitle("Choose a directory to generate");
        // Set Initial Directory
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    private void configuringFileChooser(FileChooser fileChooser) {
        // Set title for DirectoryChooser
        fileChooser.setTitle("Select YAML file");
        // Set Initial Directory
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("YAML", "*.yaml"));
    }
}
