import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class GUI{

	JFrame frame;
	JButton getFileData_btn;
	JButton downloadFile_btn;
	JLabel fileData_label;
	JTextField id_field;

	GUI(){

		frame = new JFrame(); // Window 540x240

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new GridBagLayout());


		JLabel id_label = new JLabel("ID: ");
		id_field = new JTextField();

		getFileData_btn = new JButton("Get Data");
		getFileData_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getFileData_btn_function_inNewThread();
			}
		});

		downloadFile_btn = new JButton("Download");
		downloadFile_btn.setEnabled(false);


		fileData_label = new JLabel("", SwingConstants.LEFT); // text-align: Left
		fileData_label.setBorder(BorderFactory.createLineBorder(Color.BLACK));


		// PACKING Components

		// ID LABEL
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5); // To Keep Spaces Between Components

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
		c.fill = GridBagConstraints.HORIZONTAL; // To Fill the X-axis
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







	public void getFileData_btn_function(){
		getFileData_btn.setEnabled(false);

		String vid_id = id_field.getText();
		String[] fileData = getFileData(vid_id);

		String fileData_str = "<html>File Name: " + fileData[0] + "<br>FileType: " + fileData[1] + "<br>FileSize: " + fileData[2] + "MB";
		fileData_label.setText(fileData_str);

		// Preparing The Download Button
		downloadFile_btn.setEnabled(true);
		downloadFile_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downloadFile_btn_function_inNewThread(fileData);
			}
		});

		getFileData_btn.setEnabled(true);
	}

	public void getFileData_btn_function_inNewThread(){
		new SwingWorker(){
			protected Object doInBackground() throws Exception {
				getFileData_btn_function();
				return null;
			}
		}.execute();
	}







	static String[] getFileData(String vid_id){
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


			String[] info = new String[] {name, type, size_str, sourceUrl};
//			info[0] = name;
//			info[1] = type;
//			info[2] = size_str;
//			info[3] = sourceUrl;

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







	static void download(String[] fileData){
		String sourceUrl = fileData[3];
		String fileName = fileData[0];

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

	public void downloadFile_btn_function(String[] fileData){
		String oldText = fileData_label.getText();
		fileData_label.setText(oldText + "<br>Downloading..");

		downloadFile_btn.setEnabled(false);
		getFileData_btn.setEnabled(false);

		download(fileData);

		fileData_label.setText(oldText + "<br>Downloaded!");
		getFileData_btn.setEnabled(true);
	}

	public void downloadFile_btn_function_inNewThread(String[] fileData){
		new SwingWorker(){
			protected Object doInBackground() throws Exception {
				downloadFile_btn_function(fileData);
				return null;
			}
		}.execute();
	}



	public static void main(String[] args) {
		new GUI();
	}
}
