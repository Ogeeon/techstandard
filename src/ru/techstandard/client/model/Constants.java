package ru.techstandard.client.model;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	// Виды словарей в таблице Dictionaries
	public static final int DICT_CLIENTS = 0;
	public static final int DICT_WORKSUBJS = 1;
	public static final int DICT_OBJTYPES = 2;
	public static final int DICT_CHECKERS = 3;
	public static final int DICT_ATTACHTYPES = 4;
	public static final int DICT_PROB_CLIENTS = 5;
	public static final int DICT_EVAL_FIELDS = 6;
	public static final int DICT_POSITIONS = 7;
	public static final int DICT_TASKTYPES = 8;
	
	// Действия для окошек редактирования объектов
	public static final int ACTION_ADD = 1;
	public static final int ACTION_UPDATE = 2;
	public static final int ACTION_EDIT = 3;

	// Виды приложений для поля attachments.parent_type
	public static final int ACT_ATTACHMENTS = 1;
	public static final int TASK_ATTACHMENTS = 2;
	public static final int DEVICE_ATTACHMENTS = 3;
	public static final int CONTRACT_ATTACHMENTS = 4;
	public static final int GUIDE_ATTACHMENTS = 5;
	public static final int REQUEST_ATTACHMENTS = 6;
	public static final int EVAL_ATTACHMENTS = 7;
	
	// Виды журналов
	public static final int JOURN_ACTS = 0;
	public static final int JOURN_CONTRACTS = 1;
	public static final int JOURN_GUIDES = 2;
	public static final int JOURN_REQUESTS = 3;
	
	// Статус пользователя для заданий
	public enum Privilege {USER, BOSS, ADMIN};
	
	// Права доступа
	public static final int ACCESS_READ = 1;   // 00001
	public static final int ACCESS_UPDATE = 2; // 00010
	public static final int ACCESS_INSERT = 4; // 00100
	public static final int ACCESS_DELETE = 8; // 01000
	public static final int ACCESS_PRINT = 16; // 10000
	
	public static final Map<Integer, String> OPERATION_NAMES;
	static
    {
		OPERATION_NAMES = new HashMap<Integer, String>();
		OPERATION_NAMES.put(ACCESS_READ, "Чтение");
		OPERATION_NAMES.put(ACCESS_UPDATE, "Изменение");
		OPERATION_NAMES.put(ACCESS_INSERT, "Вставка");
		OPERATION_NAMES.put(ACCESS_DELETE, "Удаление");
		OPERATION_NAMES.put(ACCESS_PRINT, "Печать");
    }
	
	// Ключи для коллекций - разделы программы
	public static final String[] SECTION_KEYS = {"acts", "contracts", "guides", "requests", "devices", "dictionaries", "evaluations", "employees", "rights"};
	
	public static final Map<String, String> SECTION_NAMES;
	static
    {
		SECTION_NAMES = new HashMap<String, String>();
		SECTION_NAMES.put("acts", "Экспертизы, акты");
		SECTION_NAMES.put("contracts", "Договоры");
		SECTION_NAMES.put("guides", "Руководства, паспорта");
		SECTION_NAMES.put("requests", "Прочие задачи");
		SECTION_NAMES.put("devices", "Поверки");
		SECTION_NAMES.put("evaluations", "Аттестация");
		SECTION_NAMES.put("dictionaries", "Справочники");
		SECTION_NAMES.put("employees", "Сотрудники");
		SECTION_NAMES.put("rights", "Права доступа");
    }

	// Список таблиц
	public static final String[] TABLES = {"acts", "clients", "contracts", "devices", "dictionaries", "guides", "requests"};
	
	// Фрагмент xml для формирования типового договора
	public static final String LINEBREAK="</w:t></w:r></w:p><w:p w:rsidR='00553263' w:rsidRPr='007D716E' w:rsidRDefault='007D716E' w:rsidP='002F6944'><w:pPr><w:pStyle w:val='a3'/><w:spacing w:after='0' w:line='240' w:lineRule='auto'/><w:ind w:left='0'/><w:jc w:val='both'/><w:rPr><w:rFonts w:ascii='Times New Roman' w:hAnsi='Times New Roman'/><w:b/><w:i/><w:sz w:val='24'/><w:szCs w:val='24'/><w:lang w:val='en-US'/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:ascii='Times New Roman' w:hAnsi='Times New Roman'/><w:b/><w:i/><w:sz w:val='24'/><w:szCs w:val='24'/><w:lang w:val='en-US'/></w:rPr><w:t>";

	public static final int ADMIN_ID = 1;
}
