package org.toxsoft.l2.dlms.virtdata;

import ru.toxsoft.l2.core.dlm.IDlm;
import ru.toxsoft.l2.core.dlm.IDlmContext;
import ru.toxsoft.l2.core.dlm.impl.AbstractDlmFactory;

/**
 * Фабрика модуля для выполнения пользовательских правил
 *
 * @author dima
 */
public class DlmFactory
    extends AbstractDlmFactory {

  @Override
  protected IDlm doCreateDlm( IDlmContext aContext ) {
    return new UserRulesExecutionDlm( info(), aContext );
  }
}
