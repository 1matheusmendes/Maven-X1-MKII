package com.br.phdev;

import com.br.phdev.cmp.*;
import com.br.phdev.driver.Module;
import com.br.phdev.driver.PCA9685;
import com.br.phdev.exceptions.MavenDataException;
import com.br.phdev.misc.Log;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Scanner;


public class Maven {

	private enum Error {
		SYSTEM_NOT_STARTED, ERROR_ON_LOAD_DATA, INVALID_COMMAND, INVALID_INPUT, SERVO_NOT_FOUND, COMMAND_DISABLED
	}

	private List<Module> moduleList;
	private List<ServoData> servoDataList;
	private List<LegData> legDataList;

	private Leg[] legs;
	private Servo[] servos;

	private void initSystem() throws I2CFactory.UnsupportedBusNumberException {
		if (this.loadData()) {
			for (Module module : moduleList) {
				module.init();
				if (module instanceof PCA9685)
					((PCA9685) module).setPWMFreq(60);
			}
			this.initLegs();
		} else
			Log.e("Falha ao iniciar o sistema");
	}

	private boolean loadData() {
		DataRepo dataRepo = new DataRepo();
		try {
			Log.i("Carregando informações para os módulos...");
			this.moduleList = dataRepo.loadModulesData();
			Log.i("Carregando informações para os servos...");
			this.servoDataList = dataRepo.loadServosData();
			Log.i("Carregando informações para as pernas...");
			this.legDataList = dataRepo.loadLegsData();
		} catch (MavenDataException e) {
			Log.e("Falha ao carregar as informações. " + e.getMessage());
			return false;
		}
		Log.s("Informações carregadas");
		return true;
	}

	private void initLegs() {
		try {
			this.legs = new Leg[legDataList.size()];
			this.servos = new Servo[servoDataList.size()];

			Log.i("Definindo os dados para todos os componentes...");
			for (int i=0; i<legDataList.size(); i++) {
				Base base = null;
				Femur femur = null;
				Tarsus tarsus = null;

				for (ServoData servoData : servoDataList) {

					if (legDataList.get(i).getBaseServo() == servoData.getGlobalChannel()) {

						this.servos[servoData.getGlobalChannel()] = new Servo((PCA9685) Module.getModule(this.moduleList, servoData.getModuleAddress()), servoData, 0);
						base = new Base(
								this.servos[servoData.getGlobalChannel()]
						);
						Log.s("Servo da base da perna " + i + " carregado");
					}
					if (legDataList.get(i).getFemurServo() == servoData.getGlobalChannel()) {
						this.servos[servoData.getGlobalChannel()] = new Servo((PCA9685) Module.getModule(this.moduleList, servoData.getModuleAddress()), servoData, 0);
						femur = new Femur(
								this.servos[servoData.getGlobalChannel()]
						);
						Log.s("Servo do femur da perna " + i + " carregado");
					}
					if (legDataList.get(i).getTarsusServo() == servoData.getGlobalChannel()) {
						this.servos[servoData.getGlobalChannel()] = new Servo((PCA9685) Module.getModule(this.moduleList, servoData.getModuleAddress()), servoData, 0);
						tarsus = new Tarsus(
								this.servos[servoData.getGlobalChannel()]
						);
						Log.s("Servo do tarso da perna " + i + " carregado");
					}
					legs[i] = new Leg(legDataList.get(i), base, femur, tarsus);
				}
				if (base == null || femur == null || tarsus == null)
					throw new RuntimeException("Falha ao inicializar as pernas");
			}
			Log.i("Dados de todos os componentes definidos com sucesso");
		} catch (Exception e) {
			Log.e("Falha ao inicializar as pernas. " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean findServo(int globalChannel) {
		for (Servo servo : this.servos) {
			if (globalChannel == servo.getServoData().getGlobalChannel())
				return true;
		}
		return false;
	}

	private List<Module> getModuleList() {
		return this.moduleList;
	}

	private Servo[] getServos() {
		return servos;
	}

	private Leg[] getLegs() {
		return this.legs;
	}

	private static void waitFor(long howMuch) {
		try {
			Thread.sleep(howMuch);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}


	public static void main(String[] args) {
		try {
			Maven maven = new Maven();

			Scanner in = new Scanner(System.in);

			boolean runningProgram = true;
			boolean initSystem = false;
			while (runningProgram) {

				String currentPath = "";
				System.out.print(currentPath + "> ");
				String command = in.nextLine();
				switch (command.trim()) {
					case "exit":
						runningProgram = false;
						break;
					case "init-system":
						maven.initSystem();
						initSystem = true;
						break;
					case "configure-legs":
						break;
					case "configure-servos": {
						if (initSystem) {
							boolean runningAllServosConfig = true;
							while (runningAllServosConfig) {
								currentPath = "configure-servos ";
								System.out.print(currentPath + "> ");
								String parameter = in.nextLine();
								switch (parameter) {
									case "exit":
										runningAllServosConfig = false;
										break;
									case "exit-f":
										runningAllServosConfig = false;
										runningProgram = false;
										break;
									case "show-s":
										showTHIS(maven.getLegs());
										break;
									default: {
										try {
											int globalChannel;
											if (parameter.startsWith("servo ")) {
												int indexOfMark = parameter.indexOf("-");
												globalChannel = Integer.parseInt(parameter.substring(6, indexOfMark != -1 ? indexOfMark -1 : parameter.length()));
												if (maven.findServo(globalChannel)) {
													if (parameter.endsWith(" -show")) {
														Log.i(maven.getServos()[globalChannel].getServoData().toString());
													} else if (parameter.endsWith(" -min")) {
														maven.getServos()[globalChannel].moveToMin();
														//showError(Error.COMMAND_DISABLED);
													} else if (parameter.endsWith(" -mid")) {
														maven.getServos()[globalChannel].moveToMid();
														//showError(Error.COMMAND_DISABLED);
													} else if (parameter.endsWith(" -max")) {
														maven.getServos()[globalChannel].moveToMax();
														//showError(Error.COMMAND_DISABLED);
													} else {
														boolean runningServoConfig = true;
														while (runningServoConfig) {
															currentPath = "configure-servos (servo " + globalChannel + ") ";
															System.out.print(currentPath + "> ");
															parameter = in.nextLine();
															switch (parameter.trim()) {
																case "min":
																case "mid":
																case "max": {
																	String currentServoConfigName = parameter.trim();
																	boolean runningServoPosConfig = true;
																	float servoPos = -1;
																	while (runningServoPosConfig) {
																		currentPath = "configure-servos (servo " + globalChannel + " - " + currentServoConfigName + ") ";
																		System.out.print(currentPath + "> ");
																		try {
																			parameter = in.nextLine();
																			switch (parameter.trim()) {
																				case "save":
																					if (servoPos != -1) {
																						DataRepo dataRepo = new DataRepo();
																						dataRepo.saveServoPosData(globalChannel, currentServoConfigName, servoPos);
																						Log.s("A informação foi salva");
																						if (currentServoConfigName.equals("min"))
																							maven.getServos()[globalChannel].getServoData().setMinPosition(servoPos);
																						else if (currentServoConfigName.equals("mid"))
																							maven.getServos()[globalChannel].getServoData().setMidPosition(servoPos);
																						else if (currentServoConfigName.equals("max"))
																							maven.getServos()[globalChannel].getServoData().setMaxPosition(servoPos);
																					} else
																						Log.w("Os dados não foram alterados");
																					runningServoPosConfig = false;
																					break;
																				case "exit":
																					runningServoPosConfig = false;
																					break;
																				case "exit-f":
																					runningAllServosConfig = false;
																					runningProgram = false;
																					runningServoConfig = false;
																					runningServoPosConfig = false;
																					break;
																				default:
																					try {
																						servoPos = Float.parseFloat(parameter);
																						if (servoPos >= 100 && servoPos <= 650 || servoPos == 0)
																							maven.getServos()[globalChannel].setRawPosition(servoPos);
																						else
																							servoPos = -1;
																					} catch (Exception e) {
																						showError(Error.INVALID_INPUT);
																					}
																					break;
																			}
																		} catch (Exception e) {
																			showError(Error.INVALID_INPUT);
																		}
																	}
																	break;
																}
																case "limit-min":
																case "limit-max": {
																	String currentServoConfigName = parameter.trim();
																	boolean runningServoPosConfig = true;
																	int servoPos = -1;
																	while (runningServoPosConfig) {
																		currentPath = "configure-servos (servo " + globalChannel + " - " + currentServoConfigName + ") ";
																		System.out.print(currentPath + "> ");
																		try {
																			parameter = in.nextLine();
																			switch (parameter.trim()) {
																				case "save":
																					if (servoPos != -1) {
																						DataRepo dataRepo = new DataRepo();
																						dataRepo.saveServoPosData(globalChannel, currentServoConfigName, servoPos);
																						Log.s("A informação foi salva");
																						if (currentServoConfigName.equals("limit-min"))
																							maven.getServos()[globalChannel].getServoData().setLimitMin(servoPos);
																						else if (currentServoConfigName.equals("limit-max"))
																							maven.getServos()[globalChannel].getServoData().setLimitMax(servoPos);
																					} else
																						Log.w("Os dados não foram alterados");
																					runningServoPosConfig = false;
																					break;
																				case "exit":
																					runningServoPosConfig = false;
																					break;
																				case "exit-f":
																					runningAllServosConfig = false;
																					runningProgram = false;
																					runningServoConfig = false;
																					runningServoPosConfig = false;
																					break;
																				default:
																					try {
																						servoPos = Integer.parseInt(parameter);
																						if (servoPos >= -90 && servoPos <= 90)
																							maven.getServos()[globalChannel].move(servoPos);
																						else
																							servoPos = -1;
																					} catch (Exception e) {
																						showError(Error.INVALID_INPUT);
																					}
																					break;
																			}
																		} catch (Exception e) {
																			showError(Error.INVALID_INPUT);
																		}
																	}
																	break;
																}
																case "general-values":
																	currentPath = "configure-servos (servo " + globalChannel + " - general values) ";
																	System.out.println(currentPath + "> ");
																	break;
																case "exit":
																	runningServoConfig = false;
																	break;
																case "show":
																	Log.i(maven.getServos()[globalChannel].getServoData().toString());
																	break;
																case "exit-f":
																	runningAllServosConfig = false;
																	runningProgram = false;
																	runningServoConfig = false;
																	break;
																default:
																	showError(Error.INVALID_COMMAND);
																	break;
															}
														}
													}
												} else {
													showError(Error.SERVO_NOT_FOUND);
												}
											}
										} catch (Exception e) {
											showError(Error.INVALID_INPUT);
										}
										break;
									}
								}
							}
						} else
							showError(Error.SYSTEM_NOT_STARTED);
						break;
					}
					case "system-restart-modules":
						for (Module module : maven.getModuleList()) {
							if (module instanceof PCA9685)
								module.restartDriver();
						}
						break;
					case "run-script":
						boolean runningScript = true;
						while (runningScript) {
							currentPath = "run-script ";
							System.out.print(currentPath + "> ");
							String script = in.nextLine();
							switch (script) {
								case "exit":
									runningScript = false;
									break;
								default:
									boolean servoFind = false;
									StringBuilder currentServoNum = new StringBuilder();
									for (int i=0; i<script.length(); i++) {
										char c = script.charAt(i);
										if (c == 's') {
											servoFind = true;
										} else if (c == 'm') {
											int channelGlobal = Integer.parseInt(currentServoNum.toString());
											if (script.charAt(i+1) == 'i' && script.charAt(i+2) == 'n') {
												maven.getServos()[channelGlobal].moveToMin();
												i += 3;
											} else if (script.charAt(i+1) == 'i' && script.charAt(i+2) == 'd') {
												maven.getServos()[channelGlobal].moveToMid();
												i += 3;
											} else if (script.charAt(i+1) == 'a' && script.charAt(i+2) == 'x') {
												maven.getServos()[channelGlobal].moveToMax();
												i += 3;
											}
											currentServoNum = new StringBuilder();
										} else if (c == '-' && servoFind){
											servoFind = false;
										} else if (c == ' ') {

										} else if (c == '@') {
											Maven.waitFor(500);
										} else if (servoFind)
											currentServoNum.append(c);
									}
									break;
							}
						}
						break;
					case "":
						break;
					default:
						System.out.println(command);
						showError(Error.INVALID_COMMAND);
						break;
				}
			}

		} catch (I2CFactory.UnsupportedBusNumberException e) {
			Log.e(e.getMessage());
			//e.printStackTrace();
		} catch (IOException e) {
			Log.e(e.getMessage());
			//e.printStackTrace();
		}
	}

	private static void showError(Error error) {
		switch (error) {
			case SYSTEM_NOT_STARTED:
				Log.e("Sistema não iniciado");
				break;
			case INVALID_COMMAND:
				Log.e("Comando inválido");
				break;
			case INVALID_INPUT:
				Log.e("Entrada inválida");
				break;
			case SERVO_NOT_FOUND:
				Log.e("Servo não encontrado");
				break;
			case COMMAND_DISABLED:
				Log.e("Comando temporiariamente desabilitado");
				break;
		}
	}

	private static void showTHIS(Leg[] legs) {
		int l1t = legs[0].getTarsus().getServo().getServoData().getGlobalChannel();
		String l1f = legs[0].getFemur().getServo().getServoData().getGlobalChannel() + "";
		String l1b = legs[0].getBase().getServo().getServoData().getGlobalChannel() + "";
		System.out.println();
		System.out.println();
		System.out.println("                           FRONT                    ");
		System.out.println();
		System.out.println("       _____                                   _____");
		System.out.println("      |     |                                 |     |");
		System.out.println("      |  " + s_(legs[0].getTarsus().getServo().getServoData().getGlobalChannel())  +
				" |                                 |  " + s_(legs[1].getTarsus().getServo().getServoData().getGlobalChannel()) + " |");
		System.out.println("      |_____|                                 |_____|");
		System.out.println("             o                               o");
		System.out.println("              o                             o");
		System.out.println("               o_____                 _____o");
		System.out.println("               |     |               |     |");
		System.out.println("               |  " + s_(legs[0].getFemur().getServo().getServoData().getGlobalChannel()) +
				" |               |  " + s_(legs[1].getFemur().getServo().getServoData().getGlobalChannel()) + " |");
		System.out.println("               |_____|_____     _____|_____|");
		System.out.println("                     |     |mmm|     |");
		System.out.println("                     | " + s_(legs[0].getBase().getServo().getServoData().getGlobalChannel()) +
				"  |   |  " + s_(legs[1].getBase().getServo().getServoData().getGlobalChannel()) + " |");
		System.out.println("                     |_____|   |_____|");
		System.out.println("                       m           m");
		System.out.println("                       m M A V E N m");
		System.out.println("        LEFT           m           m              RIGHT");
		System.out.println("                       m           m");
		System.out.println("   _____       _____  _m___     ___m_ _____       _____");
		System.out.println("  |     |     |     ||     |   |     |     |     |     |");
		System.out.println("  | " + s_(legs[2].getTarsus().getServo().getServoData().getGlobalChannel()) +
				"  |o o o| " + s_(legs[2].getFemur().getServo().getServoData().getGlobalChannel()) +
				"  || " + s_(legs[2].getBase().getServo().getServoData().getGlobalChannel()) + "  |   | " +
				s_(legs[3].getBase().getServo().getServoData().getGlobalChannel())+"  | " +
				s_(legs[3].getFemur().getServo().getServoData().getGlobalChannel())+"  |o o o| " +
				s_(legs[3].getTarsus().getServo().getServoData().getGlobalChannel())+"  |");
		System.out.println("  |_____|     |_____||_____|   |_____|_____|     |_____|");
		System.out.println("                       m           m");
		System.out.println("                       m           m");
		System.out.println("                      _m___     ___m_");
		System.out.println("                     |     |   |     |");
		System.out.println("                     | " + s_(legs[4].getBase().getServo().getServoData().getGlobalChannel()) +
				"  |   | " + s_(legs[5].getBase().getServo().getServoData().getGlobalChannel()) + "  |");
		System.out.println("                _____|_____|mmm|_____|_____");
		System.out.println("               |     |               |     |");
		System.out.println("               | " + s_(legs[4].getFemur().getServo().getServoData().getGlobalChannel()) +
				"  |               | " + s_(legs[5].getFemur().getServo().getServoData().getGlobalChannel()) + "  |");
		System.out.println("               |_____|               |_____|");
		System.out.println("               o                            o");
		System.out.println("              o                              o");
		System.out.println("       _____ o                                o_____");
		System.out.println("      |     |                                 |     |");
		System.out.println("      | " +s_(legs[4].getTarsus().getServo().getServoData().getGlobalChannel()) +
				"  |                                 | " + s_(legs[5].getTarsus().getServo().getServoData().getGlobalChannel()) + "  |");
		System.out.println("      |_____|                                 |_____|");
		System.out.println();
		System.out.println();
		System.out.println("                           BACK                      ");
		System.out.println();
	}

	private static String s_(int value) {
		if (value > 9)
			return value + "";
		return value + " ";
	}

}
