/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.security.tests.support.cert;

import java.security.cert.PolicyNode;
import java.util.*;

public class PolicyNodeImpl implements PolicyNode {
    
    private static final String ANY_POLICY = "2.5.29.32.0";
    private PolicyNodeImpl mParent;
    private HashSet mChildren;
    private String mValidPolicy;
    private HashSet mQualifierSet;
    private boolean mCriticalityIndicator;
    private HashSet mExpectedPolicySet;
    private boolean mOriginalExpectedPolicySet;
    private int mDepth;
    private boolean isImmutable;

    public PolicyNodeImpl(PolicyNodeImpl policynodeimpl, String s, Set set, 
                   boolean flag, Set set1, boolean flag1) {
        isImmutable = false;
        mParent = policynodeimpl;
        mChildren = new HashSet();
        if(s != null) {
            mValidPolicy = s;
        } else {
            mValidPolicy = "";
        }
        if(set != null) {
            mQualifierSet = new HashSet(set);
        } else {
            mQualifierSet = new HashSet();
        }
        mCriticalityIndicator = flag;
        if(set1 != null) {
            mExpectedPolicySet = new HashSet(set1);
        } else {
            mExpectedPolicySet = new HashSet();
        }
        mOriginalExpectedPolicySet = !flag1;
        if(mParent != null) {
            mDepth = mParent.getDepth() + 1;
            mParent.addChild(this);
        } else {
            mDepth = 0;
        }
    }

    PolicyNodeImpl(PolicyNodeImpl policynodeimpl, 
                   PolicyNodeImpl policynodeimpl1) {
        this(policynodeimpl, policynodeimpl1.mValidPolicy, ((Set) (policynodeimpl1.mQualifierSet)), policynodeimpl1.mCriticalityIndicator, ((Set) (policynodeimpl1.mExpectedPolicySet)), false);
    }

    public PolicyNode getParent() {
        return mParent;
    }

    public Iterator getChildren() {
        return Collections.unmodifiableSet(mChildren).iterator();
    }

    public int getDepth() {
        return mDepth;
    }

    public String getValidPolicy() {
        return mValidPolicy;
    }

    public Set getPolicyQualifiers() {
        return Collections.unmodifiableSet(mQualifierSet);
    }

    public Set getExpectedPolicies() {
        return Collections.unmodifiableSet(mExpectedPolicySet);
    }

    public boolean isCritical() {
        return mCriticalityIndicator;
    }

    public String toString() {
        StringBuffer stringbuffer = new StringBuffer(asString());
        for(Iterator iterator = getChildren(); iterator.hasNext(); stringbuffer.append((PolicyNodeImpl)iterator.next()));
        return stringbuffer.toString();
    }

    boolean isImmutable() {
        return isImmutable;
    }

    void setImmutable() {
        if(isImmutable)  return;
        PolicyNodeImpl policynodeimpl;
        for(Iterator iterator = mChildren.iterator(); iterator.hasNext(); policynodeimpl.setImmutable())
            policynodeimpl = (PolicyNodeImpl)iterator.next();

        isImmutable = true;
    }

    private void addChild(PolicyNodeImpl policynodeimpl) {
        if(isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        } else {
            mChildren.add(policynodeimpl);
            return;
        }
    }

    void addExpectedPolicy(String s) {
        if(isImmutable)
            throw new IllegalStateException("PolicyNode is immutable");
        if(mOriginalExpectedPolicySet) {
            mExpectedPolicySet.clear();
            mOriginalExpectedPolicySet = false;
        }
        mExpectedPolicySet.add(s);
    }

    void prune(int i) {
        if(isImmutable)
            throw new IllegalStateException("PolicyNode is immutable");
        if(mChildren.size() == 0)
            return;
        Iterator iterator = mChildren.iterator();
        do {
            if(!iterator.hasNext())  break;
            PolicyNodeImpl policynodeimpl = (PolicyNodeImpl)iterator.next();
            policynodeimpl.prune(i);
            if(policynodeimpl.mChildren.size() == 0 && i > mDepth + 1)
                iterator.remove();
        } while(true);
    }

    void deleteChild(PolicyNode policynode) {
        if(isImmutable) {
            throw new IllegalStateException("PolicyNode is immutable");
        } else {
            mChildren.remove(policynode);
            return;
        }
    }

    PolicyNodeImpl copyTree() {
        return copyTree(null);
    }

    private PolicyNodeImpl copyTree(PolicyNodeImpl policynodeimpl) {
        PolicyNodeImpl policynodeimpl1 = new PolicyNodeImpl(policynodeimpl, this);
        PolicyNodeImpl policynodeimpl2;
        for(Iterator iterator = mChildren.iterator(); iterator.hasNext(); policynodeimpl2.copyTree(policynodeimpl1))
            policynodeimpl2 = (PolicyNodeImpl)iterator.next();

        return policynodeimpl1;
    }

    Set getPolicyNodes(int i) {
        HashSet hashset = new HashSet();
        getPolicyNodes(i, ((Set) (hashset)));
        return hashset;
    }

    private void getPolicyNodes(int i, Set set) {
        if(mDepth == i) {
            set.add(this);
        } else {
            PolicyNodeImpl policynodeimpl;
            for(Iterator iterator = mChildren.iterator(); iterator.hasNext(); policynodeimpl.getPolicyNodes(i, set))
                policynodeimpl = (PolicyNodeImpl)iterator.next();
        }
    }

    Set getPolicyNodesExpected(int i, String s, boolean flag) {
        if(s.equals("2.5.29.32.0"))
            return getPolicyNodes(i);
        else
            return getPolicyNodesExpectedHelper(i, s, flag);
    }

    private Set getPolicyNodesExpectedHelper(int i, String s, boolean flag) {
        HashSet hashset = new HashSet();
        if(mDepth < i) {
            PolicyNodeImpl policynodeimpl;
            for(Iterator iterator = mChildren.iterator(); iterator.hasNext(); hashset.addAll(policynodeimpl.getPolicyNodesExpectedHelper(i, s, flag)))
                policynodeimpl = (PolicyNodeImpl)iterator.next();

        } else if(flag) {
            if(mExpectedPolicySet.contains("2.5.29.32.0"))
                hashset.add(this);
        } else if(mExpectedPolicySet.contains(s)) {
            hashset.add(this);
        }
        return hashset;
    }

    Set getPolicyNodesValid(int i, String s) {
        HashSet hashset = new HashSet();
        if(mDepth < i) {
            PolicyNodeImpl policynodeimpl;
            for(Iterator iterator = mChildren.iterator(); iterator.hasNext(); hashset.addAll(policynodeimpl.getPolicyNodesValid(i, s)))
                policynodeimpl = (PolicyNodeImpl)iterator.next();

        } else if(mValidPolicy.equals(s)) {
            hashset.add(this);
        }
        return hashset;
    }

    private static String policyToString(String s) {
        if(s.equals("2.5.29.32.0")) {
            return "anyPolicy";
        } else {
            return s;
        }
    }

    String asString() {
        if(mParent == null)
            return "anyPolicy  ROOT\n";
        StringBuffer stringbuffer = new StringBuffer();
        int i = 0;
        for(int j = getDepth(); i < j; i++)
            stringbuffer.append("  ");

        stringbuffer.append(policyToString(getValidPolicy()));
        stringbuffer.append("  CRIT: ");
        stringbuffer.append(isCritical());
        stringbuffer.append("  EP: ");
        for(Iterator iterator = getExpectedPolicies().iterator(); iterator.hasNext(); stringbuffer.append(" ")) {
            String s = (String)iterator.next();
            stringbuffer.append(policyToString(s));
        }

        stringbuffer.append(" (");
        stringbuffer.append(getDepth());
        stringbuffer.append(")\n");
        return stringbuffer.toString();
    }
}
