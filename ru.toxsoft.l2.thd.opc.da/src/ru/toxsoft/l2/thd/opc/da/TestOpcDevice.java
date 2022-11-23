package ru.toxsoft.l2.thd.opc.da;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;

import ru.toxsoft.l2.core.hal.*;
import ru.toxsoft.l2.core.hal.devices.*;
import ru.toxsoft.l2.core.hal.devices.impl.*;
import ru.toxsoft.l2.thd.opc.*;

/**
 * Тестовый драйвер OPC
 *
 * @author Maz
 */
public class TestOpcDevice
    extends AbstractSpecificDevice
    implements IOpc2S5Bridge {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  private StringMap<ITag> tags = new StringMap<>();

  /**
   * Конструктор
   *
   * @param aId
   * @param aDescription
   * @param aErrorProcessor
   */
  public TestOpcDevice( String aId, String aDescription, IHalErrorProcessor aErrorProcessor,
      IList<OpcTagPinDefinition> aSyncTags, int aUpdatePeriod, IList<OpcTagPinDefinition> aAsyncTags,
      IList<OpcTagPinDefinition> aOutputTags ) {
    super( aId, aDescription, aErrorProcessor );

    for( OpcTagPinDefinition tagDef : aSyncTags ) {
      Tag newTag = new Tag( tagDef.tagId(), EKind.RW, tagDef.valueType() );
      tags.put( tagDef.tagId(), newTag );
    }

    for( OpcTagPinDefinition tagDef : aAsyncTags ) {
      Tag newTag = new Tag( tagDef.tagId(), EKind.RW, tagDef.valueType() );
      tags.put( tagDef.tagId(), newTag );
    }

    for( OpcTagPinDefinition tagDef : aOutputTags ) {
      Tag newTag = new Tag( tagDef.tagId(), EKind.RW, tagDef.valueType() );
      tags.put( tagDef.tagId(), newTag );
    }
  }

  @Override
  public ITag tag( String aTagId ) {
    if( tags.hasKey( aTagId ) ) {
      return tags.getByKey( aTagId );
    }

    return null;
  }

  @Override
  public IStringMap<ITag> tags() {
    return tags;
  }

  @Override
  public void readValuesFromLL()
      throws TsMultipleApparatRtException {
    //

  }

  @Override
  public void writeValuesOnLL()
      throws TsMultipleApparatRtException {
    // TODO Auto-generated method stub

  }

  @Override
  public void putInBufferOutputValues() {
    // TODO Auto-generated method stub

  }

  @Override
  public void getFromBufferInputValues() {
    for( String tagKey : tags.keys() ) {
      // if( tagKey.equals( "SIMATIC 300(1).CPU 314C-2 PtP.TP1.ALM" ) ) {
      // if( tagKey.contains( "PtP.P61.CW" ) ) {
      // Tag tag = (Tag)tags.getByKey( tagKey );
      // tag.updateVal( AvUtils.avInt( (int)(Math.random() * 1024) ) );
      // }

      if( tagKey.contains( ".P3.WS" ) ) {
        Tag tag = (Tag)tags.getByKey( tagKey );
        tag.updateVal( AvUtils.avInt( (int)(Math.random() * 1024) ) );
      }
      if( tagKey.contains( ".CV" ) ) {
        Tag tag = (Tag)tags.getByKey( tagKey );
        tag.updateVal( AvUtils.avFloat( Math.random() * 1000 ) );
      }
    }
  }

  @Override
  public void closeApparatResources() {
    // TODO Auto-generated method stub

  }

}
