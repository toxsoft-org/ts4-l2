package org.toxsoft.l2.dlms.virtdata;

/**
 * Жестко "зашитые" в коде, персистентные константы загружаемого модуля исполнителя правил
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
public interface IDlmConstants {

  /**
   * Название движка скрипта
   */
  String JAVA_SCRIPT_NAME = "JavaScript";

  //
  // -----------------------------------------------------------------------------------------------
  // Константы - имена параметров в файле конфигурации подгруаемого модуля.

  /**
   * описание правил
   */
  String RULES = "rules";

  /**
   * секция одного правила
   */
  String RULE_SECTIONS = "rule.section";

  /**
   * описание одного правила
   */
  String RULE_DEF = "rule.def";

  /**
   * текст скрипта
   */
  String RULE_SCRIPT = "rule.script";

  /**
   * таймаут
   */
  String RULE_TIMEOUT = "rule.timeout";

  /**
   * описание события
   */
  String RULE_EVENT_TEXT = "rule.event.text";

  /**
   * секция описания входных параметров
   */
  String IN_PARAM_SECTION = "in.param.section";

  /**
   * секция описания вЫходных параметров
   */
  String OUT_PARAM_SECTION = "out.param.section";

  /**
   * название входного параметра в скрипте
   */
  String IN_PARAM_SCRIPT_NAME = "in.param.script.name";

  /**
   * class id входного параметра
   */
  String IN_PARAM_CLASS_ID = "in.param.class.id";

  /**
   * obj name входного параметра
   */
  String IN_PARAM_OBJ_NAME = "in.param.obj.name";

  /**
   * data id входного параметра
   */
  String IN_PARAM_DATA_ID = "in.param.data.id";

  /**
   * название вЫходного параметра в скрипте
   */
  String OUT_PARAM_SCRIPT_NAME = "out.param.script.name";

  /**
   * class id вЫходного параметра
   */
  String OUT_PARAM_CLASS_ID = "out.param.class.id";

  /**
   * obj name вЫходного параметра
   */
  String OUT_PARAM_OBJ_NAME = "out.param.obj.name";

  /**
   * data id вЫходного параметра
   */
  String OUT_PARAM_DATA_ID = "out.param.data.id";
  /**
   * id параметра события - описание нарушения
   */
  String EVENT_DESCR_PAR_ID = "description";
  /**
   * id параметра события - on
   */
  String EVENT_ON_PAR_ID = "on";
  /**
   * id события "сработало правило"
   */
  String ERROR_EVENT_ID = "error";

}
