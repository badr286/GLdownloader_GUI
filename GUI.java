import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GUI{

	JFrame frame;

	JButton getFileData_btn;
	JButton downloadFile_btn;

	JLabel fileData_label;

	JTextField id_field;

	public GUI(){
		frame = new JFrame(); // Window 240x540
		JPanel panel = new JPanel();


		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new GridBagLayout());


		// Defining Components
		JLabel id_label = new JLabel("ID: ");
		id_field = new JTextField();

		getFileData_btn = new JButton("Get Data");
		getFileData_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getFileData_btn_function();
			}
		});

		fileData_label = new JLabel("", SwingConstants.LEFT);
		fileData_label.setBorder(BorderFactory.createLineBorder(Color.BLACK));


		downloadFile_btn = new JButton("Download");
		downloadFile_btn.setEnabled(false);



		// PACKING Components

			// ID LABEL
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);

		c.gridx = 0; c.gridy = 0;
		c.ipadx = 1;
		panel.add(id_label, c);

			// ID FIELD
		c.gridx = 1; c.gridy = 0;
		c.ipadx = 264; // Every 1 letter, gets 8 padx. and since the id length is 33 letters, 33x8 =
		panel.add(id_field, c);

			// GetData BTN
		c.gridx = 2; c.gridy = 0;
		c.ipadx = 1;
		panel.add(getFileData_btn, c);

			// FileData Label
		c.gridx = 1; c.gridy = 1;
		c.ipadx = 1;
		c.ipady = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(fileData_label, c);

			// DownloadFile btn
		c.gridx = 2; c.gridy = 2;
		c.ipadx = 1;
		c.ipady = 1;
		panel.add(downloadFile_btn, c);



		// Frame Settings
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("TITLE");

		frame.pack();
		frame.setSize(540,240);

		frame.setVisible(true);
	}


	static String fileInfo_String(String[] info){
		return "<html>File Name: " + info[0] + "<br>FileType: " + info[1] + "<br>FileSize: " + info[2] + "MB";
	}


	public void getFileData_btn_function(){

		new SwingWorker(){
			@Override
			protected Object doInBackground() throws Exception {
				String vid_id = id_field.getText();
				String[] sourceData = getSourceData(vid_id);
				String fileInfo =  fileInfo_String( sourceData );

				fileData_label.setText(fileInfo);

				// Preparing The Download Button
				downloadFile_btn.setEnabled(true);

				downloadFile_btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						new SwingWorker(){
							@Override
							protected Object doInBackground() throws Exception {

								String oldText = fileData_label.getText();
								fileData_label.setText(oldText + "<br>Downloading..");

								downloadFile_btn.setEnabled(false);
								getFileData_btn.setEnabled(false);

								download(sourceData);

								fileData_label.setText(oldText + "<br>Downloaded!");
								getFileData_btn.setEnabled(true);

								return null;
							}
						}.execute();

					}
				});

				return null;
			}
		}.execute();

	}

	// HTTPURLCONNECTION THINGS

	static String[] getSourceData(String vid_id){
		String[] info;

		try{
			String vid_url = "https://drive.google.com/uc?export=download&id=" + vid_id + "&confirm=AYE";
			HttpURLConnection connection = (HttpURLConnection) new URL(vid_url).openConnection();
			connection.setInstanceFollowRedirects(false);

			String sourceUrl = connection.getHeaderField("Location").toString();
			HttpURLConnection sourceConnection = (HttpURLConnection) new URL(sourceUrl).openConnection();

			String name = getFileName(sourceConnection);
			String type = sourceConnection.getContentType();
			int size = (sourceConnection.getContentLength() /1024/1024);
			String size_str = String.valueOf(size);


			info = new String[] {name, type, size_str, sourceUrl};
//			info[0] = name;
//			info[1] = type;
//			info[2] = size_str;
//			info[3] = sourceUrl

			return info;



		}catch(Exception e){System.out.println(e);return new String[] {};}



	}

	static String getFileName(HttpURLConnection connection){
		String content_disposition = connection.getHeaderField("Content-Disposition");
		String name = content_disposition.split(";")[1];
		name = name.replace("filename=", "").replace("\"", "");
		return name;
		//return header.split(';')[1].replace('filename=', '').replace('"', '')
	}

	static void download(String[] sourceData){
		String sourceUrl = sourceData[3];
		String fileName = sourceData[0];

		try{
			HttpURLConnection connection = (HttpURLConnection) new URL(sourceUrl).openConnection();

			InputStream inputStream = connection.getInputStream();
			FileOutputStream outputStream = new FileOutputStream(fileName);

			int BUFFERSIZE = 4086;
			byte[] buffer = new byte[BUFFERSIZE];

			while(true){
				int bytesRead = inputStream.read( buffer );
				// .read(x): GETS SOME DATA FROM inputStream itself, And Stores It In x, And Returns The Amount Of Data That Has Been Stored, Or -1 If There's No More Data

				if(bytesRead==-1){break;}

				outputStream.write(buffer, 0, bytesRead);
				//          .write(data, start offset, number of bytes to write);
			}

			outputStream.close();
			inputStream.close();
		}
		catch (Exception e){ System.out.println(e); }




	}

	public static void main(String[] args) {
		new GUI();

	}

}