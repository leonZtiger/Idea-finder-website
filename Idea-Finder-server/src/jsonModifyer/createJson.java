package jsonModifyer;

public class createJson {

	public String StringToJson(String[] values, String name, String[] nameofVal) {

		StringBuilder json = new StringBuilder("{\r\n" + "\"" + name + "\":[\r\n"+"{");

		for (int i = 0; i < values.length; i++) {
			json.append("\"" + nameofVal[i] + "\":\"" + values[i] + "\""+(i == values.length -1? "":",")+"\r\n");
		}
		json.append("}]\r\n" + "}");

		return json.toString();
	}
}
