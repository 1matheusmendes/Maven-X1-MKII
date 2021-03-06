package com.br.phdev;

import com.br.phdev.driver.PCA9685;

import com.pi4j.io.i2c.I2CFactory;

import java.util.Scanner;

public class Ajustar {

	private PCA9685 modulo1;
	private PCA9685 modulo2;
/*
	public Ajustar() throws I2CFactory.UnsupportedBusNumberException {

		modulo1 = new PCA9685(0x40);
		modulo2 = new PCA9685(0x41);
	       	modulo1.setPWMFreq(60);
		modulo2.setPWMFreq(60);

		resetAllServosPos();

	}

	private void resetAllServosPos() {
		System.out.println("Zerando sinal em todos os servos.");
		for (int i = 0; i < 16; i++) {
        		modulo1.setPWM(i, 0, 0);
        		modulo2.setPWM(i, 0, 0);
        	}
	}

	private void setServoPos(int chf, int pos){
		switch (chf) {
			case 0:
				modulo1.setPWM(1, 0, pos);
				break;
			case 1:
				modulo1.setPWM(2, 0, pos);
				break;
			case 2:
				modulo1.setPWM(3, 0, pos);
				break;
			case 3:
				modulo1.setPWM(4, 0, pos);
				break;
			case 4:
				modulo1.setPWM(15, 0, pos);
				break;
			case 5:
				modulo1.setPWM(6, 0, pos);
				break;
			case 6:
				modulo1.setPWM(7, 0, pos);
				break;
			case 7:
				modulo1.setPWM(8, 0, pos);
				break;
			case 8:
				modulo1.setPWM(9, 0, pos);
				break;
			case 9:
				modulo2.setPWM(1, 0, pos);
				break;
			case 10:
				modulo2.setPWM(2, 0, pos);
				break;
			case 11:
				modulo2.setPWM(3, 0, pos);
				break;
			case 12:
				modulo2.setPWM(4, 0, pos);
				break;
			case 13:
				modulo2.setPWM(5, 0, pos);
				break;
			case 14:
				modulo2.setPWM(6, 0, pos);
				break;
			case 15:
				modulo2.setPWM(7, 0, pos);
				break;
			case 16:
				modulo2.setPWM(8, 0, pos);
				break;
			case 17:
				modulo2.setPWM(9, 0, pos);
				break;
		}
	}

	private void delay(int tempo) {
		try {
			Thread.sleep(tempo);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException {

		try {
			Ajustar aj = new Ajustar();
			Scanner entrada = new Scanner(System.in);

			int servoChannel = 0;
			int servoPos = 0;

			while (true) {

				System.out.println("AJUSTANDO SERVO\n\n");
					System.out.println("Informe o canal do servo: ");
				servoChannel = entrada.nextInt();
				if (servoChannel == -1) {
					aj.resetAllServosPos();
					continue;
				}
					System.out.println("Informe a posicao para o servo: ");
				servoPos = entrada.nextInt();

				if (servoChannel >= 0 && servoChannel < 16) {
						System.out.println("Movendo para " + servoPos);
					if (servoPos >= 150 && servoPos <= 600)
							aj.setServoPos(servoChannel, servoPos);
					else if (servoPos == 0)
						aj.setServoPos(servoChannel, 0);
				}
			}
		} catch (Exception e) {
			System.out.println("Ocorreu um erro. ");
			e.printStackTrace();
		}
		// 375 servo middle
	}

	public static void main__(String[] args) throws I2CFactory.UnsupportedBusNumberException {

		try {
			Ajustar aj = new Ajustar();
			Scanner entrada = new Scanner(System.in);

			aj.setServoPos(8, 375);
			aj.setServoPos(7, 375);
			aj.setServoPos(6, 375);
			aj.setServoPos(10, 375);
			aj.setServoPos(12, 375);
			aj.setServoPos(14, 375);

			aj.setServoPos(8, 375);
			aj.setServoPos(7, 375);
			aj.setServoPos(6, 375);
			aj.setServoPos(10, 375);
			aj.setServoPos(12, 375);
			aj.setServoPos(14, 375);

        		while (true) {
				aj.setServoPos(8, 475);
				aj.setServoPos(7, 475);
				aj.setServoPos(6, 475);
				aj.setServoPos(10, 475);
				aj.setServoPos(12, 475);
				aj.setServoPos(14, 475);
        			aj.delay(300);

        			aj.setServoPos(8, 275);
				aj.setServoPos(7, 275);
				aj.setServoPos(6, 275);
				aj.setServoPos(10, 275);
				aj.setServoPos(12, 275);
				aj.setServoPos(14, 275);
        			aj.delay(300);
			}
		} catch (Exception e) {
			System.out.println("Ocorreu um erro. ");
			e.printStackTrace();
		}
		// 375 servo middle
	}*/

}

