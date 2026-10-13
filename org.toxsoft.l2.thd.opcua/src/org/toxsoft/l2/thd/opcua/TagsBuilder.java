package org.toxsoft.l2.thd.opcua;

import static org.toxsoft.l2.thd.opcua.IOpcConstants.*;

import org.eclipse.milo.opcua.sdk.client.*;
import org.eclipse.milo.opcua.stack.core.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.avtree.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.rwkind.*;
import org.toxsoft.l2.lib.impl.*;

public class TagsBuilder {

  /**
   * Журнал работы
   */
  private ILogger logger = LoggerWrapper.getLogger( this.getClass().getName() );

  /**
   * Opc клиент
   */
  private OpcUaClient client;

  private L2AbstractHalDevice device;

  private IAvTree cfg;

  private IStridablesListEdit<L2AbstractTag> syncTags = new StridablesList<>();

  private IStridablesListEdit<L2AbstractTag> asyncTags = new StridablesList<>();

  private IStridablesListEdit<L2AbstractTag> outputTags = new StridablesList<>();

  public TagsBuilder( OpcUaClient aClient, L2AbstractHalDevice aDevice, IAvTree aCfg ) {
    client = aClient;
    device = aDevice;
    cfg = aCfg;
  }

  public IStridablesList<L2AbstractTag> getSyncTags() {
    return syncTags;
  }

  public IStridablesList<L2AbstractTag> getAsyncTags() {
    return asyncTags;
  }

  public IStridablesList<L2AbstractTag> getOutputTags() {
    return outputTags;
  }

  public void build() {
    IAvTree groupsConfig = cfg.nodes().findByKey( GROUPS_PARAM_NAME );

    IStringMapEdit<TagCfgItem> outputNodeIds = new StringMap<>();

    for( int i = 0; i < groupsConfig.arrayLength(); i++ ) {
      IAvTree groupConfig = groupsConfig.arrayElement( i );

      if( groupConfig.structId().endsWith( OUTPUT_GROUP_DEF_POSTFIX ) ) {

        // output
        IAvTree tagsConfig = groupConfig.nodes().findByKey( OUTPUT_TAGS_PARAM_NAME );
        IList<TagCfgItem> outputTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

        for( int j = 0; j < outputTagsCfgItems.size(); j++ ) {
          TagCfgItem item = outputTagsCfgItems.get( j );
          NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );
          outputNodeIds.put( nodeId.toParseableString(), item );
        }
        logger.info( "Output group cfg: outputNodeIds size %s", String.valueOf( outputNodeIds.size() ) );
      }
    }

    for( int i = 0; i < groupsConfig.arrayLength(); i++ ) {
      IAvTree groupConfig = groupsConfig.arrayElement( i );

      if( groupConfig.structId().endsWith( SYNC_GROUP_DEF_POSTFIX ) ) {
        // synch
        IAvTree tagsConfig = groupConfig.nodes().findByKey( SYNC_TAGS_PARAM_NAME );
        IList<TagCfgItem> synchTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

        for( int j = 0; j < synchTagsCfgItems.size(); j++ ) {
          TagCfgItem item = synchTagsCfgItems.get( j );
          NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );

          try {
            client.getAddressSpace().getVariableNode( nodeId );
          }
          catch( UaException uaEx ) {
            logger.error( "Synch read tag '%s' creation faild '%s'", nodeId.toParseableString(), uaEx.getMessage() );
            continue;
          }

          boolean isOutput = outputNodeIds.hasKey( nodeId.toParseableString() );

          TagImpl tag = new TagImpl( device, nodeId.toParseableString(), new OptionSet(),
              new DataType( item.getTagType() ), isOutput ? ERwKind.RW : ERwKind.R );
          syncTags.add( tag );
          if( isOutput ) {
            outputTags.add( tag );
            outputNodeIds.removeByKey( nodeId.toParseableString() );
          }
        }
        logger.info( "Sync group: successfully formed %s tags", String.valueOf( syncTags.size() ) );
      }
      else
        if( groupConfig.structId().endsWith( ASYNC_GROUP_DEF_POSTFIX ) ) {
          // async
          IAvTree tagsConfig = groupConfig.nodes().findByKey( ASYNC_TAGS_PARAM_NAME );
          IList<TagCfgItem> asynchTagsCfgItems = OpcUaUtils.createTagsCfgItems( tagsConfig );

          for( int j = 0; j < asynchTagsCfgItems.size(); j++ ) {
            TagCfgItem item = asynchTagsCfgItems.get( j );
            NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );

            try {
              client.getAddressSpace().getVariableNode( nodeId );
            }
            catch( UaException uaEx ) {
              logger.error( "ASynch read tag '%s' creation faild '%s'", nodeId.toParseableString(), uaEx.getMessage() );
              continue;
            }

            boolean isOutput = outputNodeIds.hasKey( nodeId.toParseableString() );

            TagImpl tag = new TagImpl( device, nodeId.toParseableString(), new OptionSet(),
                new DataType( item.getTagType() ), isOutput ? ERwKind.RW : ERwKind.R );
            asyncTags.add( tag );
            if( isOutput ) {
              outputTags.add( tag );
              outputNodeIds.removeByKey( nodeId.toParseableString() );
            }
          }
        }
    }

    for( String outputId : outputNodeIds.keys() ) {
      TagCfgItem item = outputNodeIds.getByKey( outputId );
      NodeId nodeId = OpcUaUtils.createNodeFromCfg( item );
      try {
        client.getAddressSpace().getVariableNode( nodeId );
      }
      catch( UaException uaEx ) {
        logger.error( "Write tag '%s' creation faild '%s'", nodeId.toParseableString(), uaEx.getMessage() );
        continue;
      }

      TagImpl tag = new TagImpl( device, nodeId.toParseableString(), new OptionSet(), new DataType( item.getTagType() ),
          ERwKind.W );
      outputTags.add( tag );
    }
    logger.info( "Output group: successfully added %s nodes", String.valueOf( outputTags.size() ) );
  }
}
