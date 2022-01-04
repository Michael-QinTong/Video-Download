import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadVideoUI extends JFrame {

    private JTextArea URL;
    private JButton button;
    private JPanel panel;
    private JTextArea info;
    private static String videoUrl = null;
    private static final int MAX_BUFFER_SIZE = 1000000;
    private boolean flag = true;

    public static void main(String[] args) {
        DownloadVideoUI downloadlVideo = new DownloadVideoUI();
    }

    public DownloadVideoUI()  {
        URL = new JTextArea(20,10);
        URL.setLineWrap(true);
        info = new JTextArea(30,30);
        info.setEditable(false);
        info.append("工具由Michael-QinTong提供，项目完全开源\n");
        info.append("github链接:\n https://github.com/Michael-QinTong/Video-Download\n");
        info.append("抖音id:\n 1101433905\n");
        info.append("====================================\n");
        button = new JButton("发送");
        ActionListener download = new Download();
        button.addActionListener(download);
        panel = new JPanel();
        panel.add(new JLabel("消息"));
        panel.add(info);
        panel.add(new JLabel("链接"));
        panel.add(URL);
        panel.add(button);
        add(panel);
        pack();
        setTitle("Download Video Util @copyright Michael-QinTong");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
    }

    class Download implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            // TODO Auto-generated method stub
            videoUrl = URL.getText();
//            System.out.println(videoUrl);
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            RandomAccessFile randomAccessFile = null;
            try {
                // 1.获取连接对象
                URL url = new URL(videoUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Range", "bytes=0-");
                connection.connect();
                if(connection.getResponseCode() / 100 != 2) {
                    info.append("连接失败...\n");
                    info.append("====================================\n");
                    return;
                }
                // 2.获取连接对象的流
                inputStream = connection.getInputStream();
                //已下载的大小
                int downloaded = 0;
                //总文件的大小
                int fileSize = connection.getContentLength();
                String fileName = url.getFile();
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                // 3.把资源写入文件
                randomAccessFile = new RandomAccessFile(fileName,"rw");
                while(downloaded < fileSize) {
                    // 3.1设置缓存流的大小
                    byte[] buffer = null;
                    if(fileSize - downloaded >= MAX_BUFFER_SIZE) {
                        buffer = new byte[MAX_BUFFER_SIZE];
                    }else {
                        buffer = new byte[fileSize - downloaded];
                    }
                    // 3.2把每一次缓存的数据写入文件
                    int read = -1;
                    int currentDownload = 0;
                    long startTime = System.currentTimeMillis();
                    while(currentDownload < buffer.length) {
                        read = inputStream.read();
                        buffer[currentDownload ++] = (byte) read;
                    }
                    long endTime = System.currentTimeMillis();
                    double speed = 0.0;
                    if(endTime - startTime > 0) {
                        speed = currentDownload / 1024.0 / ((double)(endTime - startTime)/1000);
                    }
                    randomAccessFile.write(buffer);
                    downloaded += currentDownload;
                    randomAccessFile.seek(downloaded);
                    String str = String.format("下载了进度:%.2f%%,下载速度：%.1fkb/s(%.1fM/s)%n",downloaded * 1.0 / fileSize * 10000 / 100,speed,speed/1000);
                    if(flag){
                        Thread thread = new Thread(){
                            @Override
                            public void run() {
                                info.append(str);
                                info.paintImmediately(info.getBounds());
                                flag = false;
                                try {
                                    sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } finally {
                                    flag = true;
                                }
                            }
                        };
                        thread.start();
                    }
                }
                info.append("下载完成\n");
                info.append("====================================\n");
            } catch (MalformedURLException e) {
                info.append("连接失败...\n");
                return ;
            } catch (IOException e) {
                info.append("连接失败...\n");
                return ;
            }finally {
                try {
                    if(connection!=null)
                        connection.disconnect();
                    if(inputStream!=null)
                        inputStream.close();
                    if(randomAccessFile!=null)
                        randomAccessFile.close();
                } catch (IOException e) {
                    info.append("资源释放失败...\n");
                    return ;
                }
            }
        }
    }
}
