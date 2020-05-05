package chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.awt.Color;
// import java.io.BufferedWriter;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class GuiStudent extends JFrame implements ActionListener, KeyListener, MessageListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 763321497521696765L;

	private JPanel contentPane;

	// �ı���
	private JTextPane jta;
	// ������
	private JScrollPane jsp;
	// ���
	private JPanel jp;
	// �ı���
	private JTextField jtf;
	// ��ť
	private JButton jb;
	private SimpleAttributeSet consumerStyle;
	private SimpleAttributeSet producerStyle;
	private SimpleAttributeSet fontStyle;

	// ConnectionFactory: ���ӹ�����JMS ������������
	ConnectionFactory connectionFactory;
	// Connection: JMS �ͻ��˵�JMS Provider ������
	Connection connection = null;
	// Session: һ�����ͻ������Ϣ���߳�
	Session session1, session2;
	// Destination: ��Ϣ��Ŀ�ĵ�;��Ϣ���͸�˭.
	Destination destination1, destination2;
	// MessageProducer: ��Ϣ������
	MessageProducer producer;
	// MessageConsumer: ��Ϣ������
	MessageConsumer consumer;
	// label:��Ȩ��Ϣ
	Label label;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiStudent frame = new GuiStudent();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public GuiStudent() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 760, 720);
		setTitle("Student");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		// ��ʼ�����
		// �ı���
		jta = new JTextPane();
		jta.setEditable(false);// �����ı��򲻿��Ա༭
		// ע�⣺��Ҫ���ı�����ӵ��������ϣ�ʵ�ֹ���Ч����
		jsp = new JScrollPane(jta);
		// ���
		jp = new JPanel();

		// �ı��򣬲����Ǵ�С
		jtf = new JTextField(10);
		// ��ť
		jb = new JButton("Send");
		// ��ǩ
		label = new Label("�Źܸ��Ʒ�С��");

		// ��Ҫ���ı����밴ť��ӵ������
		jp.add(jtf);
		jp.add(jb);
		jp.add(label);

		// ����   ����������ӹ���������壬�����ǲ���λ��
		this.add(jsp, BorderLayout.CENTER);
		this.add(jp, BorderLayout.SOUTH);

		// ��ť�ﶨ�����¼�
		jb.addActionListener(this);
		jtf.addKeyListener(this);
		styleInit();

		queue();
	}

	void queue() {
		// TextMessage message;
		// ����ConnectionFactoryʵ�����󣬴˴�����ActiveMq��ʵ��jar
		connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");

		try {
			// ����ӹ����õ����Ӷ���
			connection = connectionFactory.createConnection();
			// ����
			connection.start();
			// ��ȡ��������
			session1 = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
			session2 = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
			// ��ȡsessionע�����ֵxingbo.xu-queue��һ����������queue��������ActiveMq��console����
			destination1 = session1.createQueue("Student");
			destination2 = session2.createQueue("Teacher");
			// �õ���Ϣ�����ߡ������ߡ�
			producer = session1.createProducer(destination1);
			// ���ò��־û����˴�ѧϰ��ʵ�ʸ�����Ŀ����
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			// ������Ϣ���˴�д������Ŀ���ǲ��������߷�����ȡ
			consumer = session2.createConsumer(destination2);
			// sendMessage(session, producer);
			consumer.setMessageListener(this);
			session1.commit();
			// receive();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void send() throws JMSException {
		String text = jtf.getText();
		String show_text = text + System.lineSeparator();
		String user = "Student: " + getTime() + System.lineSeparator();
		insert(show_text, user, producerStyle);

		TextMessage message = session1.createTextMessage(text);
		// ������Ϣ��Ŀ�ĵط�
		producer.send(message);
		session1.commit();
		jtf.setText("");
	}

	private void insert(String str, String user, SimpleAttributeSet attrSet) {
		Document document = jta.getDocument();
		try {
			document.insertString(document.getLength(), user, attrSet);
			document.insertString(document.getLength(), str, fontStyle);
		} catch (BadLocationException e) {
			System.out.println("BadLocationException:" + e);
		}
	}

	private void styleInit() {
		fontStyle = new SimpleAttributeSet();
		StyleConstants.setFontFamily(fontStyle, "����");
		StyleConstants.setFontSize(fontStyle, 18);
		StyleConstants.setForeground(fontStyle, Color.black);
		producerStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(producerStyle, Color.green);
		StyleConstants.setFontSize(producerStyle, 14);
		consumerStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(consumerStyle, Color.blue);
		StyleConstants.setFontSize(consumerStyle, 14);
	}

	private String getTime() {
		Date date = new Date();
		DateFormat formatdate = DateFormat.getDateTimeInstance();
		return formatdate.format(date);
	}

	// ��������¼�
	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			send();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				send();
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(Message arg0) {
		try {
			String text = ((TextMessage) arg0).getText() + System.lineSeparator();
			String user = "Teacher: " + getTime() + System.lineSeparator();
			insert(text, user, consumerStyle);
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

}
