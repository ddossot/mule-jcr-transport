/*
 * $Id: TransformersTestCase.vm 10787 2008-02-12 18:51:50Z dfeist $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jcr.transformers;

import org.mule.transformer.AbstractTransformerTestCase;
import org.mule.api.transformer.Transformer;


public class JcrTransformersTestCase extends AbstractTransformerTestCase
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transformer.AbstractTransformerTestCase#getTestData()
     */
    public Object getTestData()
    {
        // TODO create a test data object that will be passed into the
        // transformer
        throw new UnsupportedOperationException("getResultData");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transformer.AbstractTransformerTestCase#getResultData()
     */
    public Object getResultData()
    {
        try {
            // TODO Return the result data expected once the getTestData()
            // value has been transformed
            throw new UnsupportedOperationException("getResultData");
        }
        catch (Exception ex) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transformer.AbstractTransformerTestCase#getTransformers()
     */
    public Transformer getTransformer()
    {
        Transformer t = new JcrEventToObject();
        // Set the correct return class for this roundtrip test
        t.setReturnClass(this.getResultData().getClass());
        return t;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mule.transformer.AbstractTransformerTestCase#getRoundTripTransformer()
     */
    public Transformer getRoundTripTransformer()
    {
        // No Outbound transformer was created for this Transport
        return null;
    }

}
