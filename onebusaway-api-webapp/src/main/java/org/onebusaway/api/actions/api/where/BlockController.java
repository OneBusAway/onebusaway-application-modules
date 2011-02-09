package org.onebusaway.api.actions.api.where;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.onebusaway.api.actions.api.ApiActionSupport;
import org.onebusaway.api.model.transit.BeanFactoryV2;
import org.onebusaway.api.model.transit.EntryWithReferencesBean;
import org.onebusaway.api.model.transit.blocks.BlockV2Bean;
import org.onebusaway.exceptions.ServiceException;
import org.onebusaway.transit_data.model.blocks.BlockBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.annotations.RequiredFieldValidator;

public class BlockController extends ApiActionSupport {

  private static final long serialVersionUID = 1L;

  private static final int V2 = 2;

  @Autowired
  private TransitDataService _service;

  private String _id;

  public BlockController() {
    super(V2);
  }

  @RequiredFieldValidator
  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public DefaultHttpHeaders show() throws ServiceException {

    if (!isVersion(V2))
      return setUnknownVersionResponse();

    if (hasErrors())
      return setValidationErrorsResponse();

    BlockBean block = _service.getBlockForId(_id);

    if (block == null)
      return setResourceNotFoundResponse();

    BeanFactoryV2 factory = getBeanFactoryV2();
    EntryWithReferencesBean<BlockV2Bean> response = factory.getBlockResponse(block);
    return setOkResponse(response);
  }
}
