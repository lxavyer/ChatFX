package com.leandro.chatjavafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatController extends Component  {

    @FXML
    private TextArea areaClientes;
    @FXML
    private TextArea areaMensagens;
    @FXML
    private TextField campoMensagem;
    @FXML
    private TextField campoServidor;
    @FXML
    private TextField campoPorta;
    @FXML
    private Button botaoConectar;
    @FXML
    private Button botaoEnviar;

    private String nomeCliente;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Timeline pingTimeline;

    @FXML
    public void handleBotaoConectarAction(ActionEvent event) {
        try {
            conectar();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Erro ao conectar", e.getMessage());
        }
    }

    @FXML
    public void handleBotaoEnviarAction(ActionEvent event) {
        enviarMensagem();
    }

    @FXML
    public void handleCampoMensagemAction(ActionEvent event) {
        enviarMensagem();
    }

    private void conectar() throws IOException {
        String servidor = campoServidor.getText();
        int porta = Integer.parseInt(campoPorta.getText());

        socket = new Socket(servidor, porta);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        nomeCliente = JOptionPane.showInputDialog(null, "Digite seu nome:");

        outputStream.writeChars(nomeCliente);

        new Thread(() -> {
            try {
                while (true) {
                    String mensagem = aguardaMensagem(inputStream);
                    if (mensagem.startsWith("#$#")) {
                        atualizarListaClientes(mensagem);
                    } else {
                        Platform.runLater(() -> {
                            areaMensagens.appendText(mensagem + "\n");
                        });
                    }
                }
            } catch (IOException e) {
                desconectar();
            }
        }).start();
        (new Timer(3000, new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    ChatController.this.outputStream.writeChars("#$#\n");
                } catch (IOException var3) {
                   System.out.println("Error");
                }

            }
        })).start();
    }



    private void desconectar() {
        if (pingTimeline != null) {
            pingTimeline.stop();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensagem() {
        String mensagem = this.campoMensagem.getText();
        if (!mensagem.isEmpty()) {
            try {
                this.outputStream.writeChars(mensagem + "\n");
                this.campoMensagem.setText("");
            } catch (IOException var3) {
                JOptionPane.showMessageDialog(this, "Erro ao enviar mensagem", "Erro", 0);
                var3.printStackTrace();
            }
        }
    }

    private String aguardaMensagem(DataInputStream inputStream) throws IOException {
        StringBuilder mensagem = new StringBuilder();

        char caractere;
        do {
            caractere = inputStream.readChar();
            if (caractere != '\n') {
                mensagem.append(caractere);
            }
        } while(caractere != '\n');

        return mensagem.toString();
    }

    private void atualizarListaClientes(String cliente) {
        Platform.runLater(() -> {
            areaClientes.clear();
            areaClientes.appendText(cliente.substring(3) + "\n");
        });
    }

    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
