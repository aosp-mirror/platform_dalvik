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

/**
* @author Alexey V. Varlamov
* @version $Revision$
*/

package java.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.security.fortress.PolicyUtils;
import org.apache.harmony.security.internal.nls.Messages;

/**
 * An {@code UnresolvedPermission} represents a {@code Permission} whose type
 * should be resolved lazy and not during initialization time of the {@code
 * Policy}. {@code UnresolvedPermission}s contain all information to be replaced
 * by a concrete typed {@code Permission} right before the access checks are
 * performed.
 * 
 * @since Android 1.0
 */
public final class UnresolvedPermission extends Permission
    implements Serializable {

    private static final long serialVersionUID = -4821973115467008846L;

    private static final ObjectStreamField serialPersistentFields[] = {
        new ObjectStreamField("type", String.class), //$NON-NLS-1$
        new ObjectStreamField("name", String.class), //$NON-NLS-1$
        new ObjectStreamField("actions", String.class), }; //$NON-NLS-1$

    // Target name
    private transient String targetName;

    //Target actions
    private transient String targetActions;

    // The signer certificates 
    private transient Certificate[] targetCerts;

    // Cached hash value
    private transient int hash;

    /**
     * Constructs a new instance of {@code UnresolvedPermission}. The supplied
     * parameters are used when this instance is resolved to the concrete
     * {@code Permission}.
     * 
     * @param type
     *            the fully qualified class name of the permission this class is
     *            resolved to.
     * @param name
     *            the name of the permission this class is resolved to, maybe
     *            {@code null}.
     * @param actions
     *            the actions of the permission this class is resolved to, maybe
     *            {@code null}.
     * @param certs
     *            the certificates of the permission this class is resolved to,
     *            maybe {@code null}.
     * @throws NullPointerException
     *             if type is {@code null}.
     * @since Android 1.0
     */
    public UnresolvedPermission(String type, String name, String actions,
                                Certificate[] certs) {
        super(type);
        checkType(type);
        targetName = name;
        targetActions = actions;
        if (certs != null && certs.length != 0) {
            //TODO filter non-signer certificates ???
            List tmp = new ArrayList();
            for (int i = 0; i < certs.length; i++) {
                if (certs[i] != null) {
                    tmp.add(certs[i]);
                }
            }
            if (tmp.size() != 0) {
                targetCerts = (Certificate[])tmp.toArray(
                                new Certificate[tmp.size()]);
            }
        }
        hash = 0;
    }

    // Check type parameter
    private final void checkType(String type) {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.2F")); //$NON-NLS-1$
        }

        // type is the class name of the Permission class.
        // Empty string is inappropriate for class name.
        // But this check is commented out for compatibility with RI.
        // see JIRA issue HARMONY-733
        // if (type.length() == 0) {
        //     throw new IllegalArgumentException("type cannot be empty");
        // }
    }

    /**
     * Compares the specified object with this {@code UnresolvedPermission} for
     * equality and returns {@code true} if the specified object is equal,
     * {@code false} otherwise. To be equal, the specified object needs to be an
     * instance of {@code UnresolvedPermission}, the two {@code
     * UnresolvedPermission}s must refer to the same type and must have the same
     * name, the same actions and certificates.
     * 
     * @param obj
     *            object to be compared for equality with this {@code
     *            UnresolvedPermission}.
     * @return {@code true} if the specified object is equal to this {@code
     *         UnresolvedPermission}, otherwise {@code false}.
     * @since Android 1.0
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof UnresolvedPermission) {
            UnresolvedPermission that = (UnresolvedPermission)obj;
            if (getName().equals(that.getName())
                && (targetName == null ? that.targetName == null 
                    : targetName.equals(that.targetName))
                && (targetActions == null ? that.targetActions == null
                    : targetActions.equals(that.targetActions))
                && (PolicyUtils.matchSubset(targetCerts, that.targetCerts) 
                    && PolicyUtils.matchSubset(that.targetCerts, targetCerts))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the hash code value for this {@code UnresolvedPermission}.
     * Returns the same hash code for {@code UnresolvedPermission}s that are
     * equal to each other as required by the general contract of
     * {@link Object#hashCode}.
     * 
     * @return the hash code value for this {@code UnresolvedPermission}.
     * @see Object#equals(Object)
     * @see UnresolvedPermission#equals(Object)
     * @since Android 1.0
     */
    public int hashCode() {
        if (hash == 0) {
            hash = getName().hashCode();
            if (targetName != null) {
                hash ^= targetName.hashCode();
            }
            if (targetActions != null) {
                hash ^= targetActions.hashCode();
            }
        }
        return hash;
    }

    /**
     * Returns an empty string since there are no actions allowed for {@code
     * UnresolvedPermission}. The actions, specified in the constructor, are
     * used when the concrete permission is resolved and created.
     * 
     * @return an empty string, indicating that there are no actions.
     * @since Android 1.0
     */
    public String getActions() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the name of the permission this {@code UnresolvedPermission} is
     * resolved to.
     * 
     * @return the name of the permission this {@code UnresolvedPermission} is
     *         resolved to.
     * @since Android 1.0
     */
    public String getUnresolvedName() {
        return targetName;
    }

    /**
     * Returns the actions of the permission this {@code UnresolvedPermission}
     * is resolved to.
     * 
     * @return the actions of the permission this {@code UnresolvedPermission}
     *         is resolved to.
     * @since Android 1.0
     */
    public String getUnresolvedActions() {
        return targetActions;
    }

    /**
     * Returns the fully qualified class name of the permission this {@code
     * UnresolvedPermission} is resolved to.
     * 
     * @return the fully qualified class name of the permission this {@code
     *         UnresolvedPermission} is resolved to.
     * @since Android 1.0
     */
    public String getUnresolvedType() {
        return super.getName();
    }

    /**
     * Returns the certificates of the permission this {@code
     * UnresolvedPermission} is resolved to.
     * 
     * @return the certificates of the permission this {@code
     *         UnresolvedPermission} is resolved to.
     * @since Android 1.0
     */
    public Certificate[] getUnresolvedCerts() {
        if (targetCerts != null) {
            Certificate[] certs = new Certificate[targetCerts.length];
            System.arraycopy(targetCerts, 0, certs, 0, certs.length);
            return certs;
        }
        return null;
    }

    /**
     * Indicates whether the specified permission is implied by this {@code
     * UnresolvedPermission}. {@code UnresolvedPermission} objects imply nothing
     * since nothing is known about them yet.
     * <p>
     * Before actual implication checking, this method tries to resolve
     * UnresolvedPermissions (if any) against the passed instance. Successfully
     * resolved permissions (if any) are taken into account during further
     * processing.
     * </p>
     * 
     * @param permission
     *            the permission to check.
     * @return always {@code false}
     * @since Android 1.0
     */
    public boolean implies(Permission permission) {
        return false;
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * {@code UnresolvedPermission} including its target name and its target
     * actions.
     * 
     * @return a printable representation for this {@code UnresolvedPermission}.
     * @since Android 1.0
     */
    public String toString() {
        return "(unresolved " + getName() + " " + targetName + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + targetActions + ")"; //$NON-NLS-1$
    }

    /**
     * Returns a new {@code PermissionCollection} for holding {@code
     * UnresolvedPermission} objects.
     * 
     * @return a new PermissionCollection for holding {@code
     *         UnresolvedPermission} objects.
     * @since Android 1.0
     */
    public PermissionCollection newPermissionCollection() {
        return new UnresolvedPermissionCollection();
    }

    /**
     * Tries to resolve this permission into the specified class.
     * <p>
     * It is assumed that the class has a proper name (as returned by {@code
     * getName()} of this unresolved permission), so no check is performed to
     * verify this. However, the class must have all required certificates (as
     * per {@code getUnresolvedCerts()}) among the passed collection of signers.
     * If it does, a zero, one, and/or two-argument constructor is tried to
     * instantiate a new permission, which is then returned.
     * </p>
     * <p>
     * If an appropriate constructor is not available or the class is improperly
     * signed, {@code null} is returned.
     * </p>
     * 
     * @param targetType
     *            - a target class instance, must not be {@code null}
     * @param signers
     *            - actual signers of the targetType
     * @return resolved permission or null
     */
    Permission resolve(Class targetType) {
        // check signers at first
        if (PolicyUtils.matchSubset(targetCerts, targetType.getSigners())) {
            try {
                return PolicyUtils.instantiatePermission(targetType,
                                                         targetName,
                                                         targetActions);
            } catch (Exception ignore) {
                //TODO log warning?
            }
        }
        return null;
    }

    /**
     * @com.intel.drl.spec_ref
     * 
     * Outputs {@code type},{@code name},{@code actions}
     * fields via default mechanism; next manually writes certificates in the
     * following format: <br>
     *
     * <ol>
     * <li> int : number of certs or zero </li>
     * <li> each cert in the following format
     *     <ol>
     *     <li> String : certificate type </li>
     *     <li> int : length in bytes of certificate </li>
     *     <li> byte[] : certificate encoding </li>
     *     </ol>
     * </li>
     * </ol>
     *
     *  @see  <a href="http://java.sun.com/j2se/1.5.0/docs/api/serialized-form.html#java.security.UnresolvedPermission">Java Spec</a>
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("type", getUnresolvedType()); //$NON-NLS-1$
        fields.put("name", getUnresolvedName()); //$NON-NLS-1$
        fields.put("actions", getUnresolvedActions()); //$NON-NLS-1$
        out.writeFields();
        if (targetCerts == null) {
            out.writeInt(0);
        } else {
            out.writeInt(targetCerts.length);
            for (int i = 0; i < targetCerts.length; i++) {
                try {
                    byte[] enc = targetCerts[i].getEncoded();
                    out.writeUTF(targetCerts[i].getType());
                    out.writeInt(enc.length);
                    out.write(enc);
                } catch (CertificateEncodingException cee) {
                    throw ((IOException)new NotSerializableException(
                        Messages.getString("security.30",  //$NON-NLS-1$
                        targetCerts[i])).initCause(cee));
                }
            }
        }
    }

    /** 
     * @com.intel.drl.spec_ref
     * 
     * Reads the object from stream and checks target type for validity. 
     */
    private void readObject(ObjectInputStream in) throws IOException,
        ClassNotFoundException {
        checkType(getUnresolvedType());
        ObjectInputStream.GetField fields = in.readFields();
        if (!getUnresolvedType().equals(fields.get("type", null))) { //$NON-NLS-1$
            throw new InvalidObjectException(Messages.getString("security.31")); //$NON-NLS-1$
        }
        targetName = (String)fields.get("name", null); //$NON-NLS-1$
        targetActions = (String)fields.get("actions", null); //$NON-NLS-1$
        int certNumber = in.readInt();
        if (certNumber != 0) {
            targetCerts = new Certificate[certNumber];
            for (int i = 0; i < certNumber; i++) {
                try {
                    String type = in.readUTF();
                    int length = in.readInt();
                    byte[] enc = new byte[length];
                    in.readFully(enc, 0, length);
                    targetCerts[i] = CertificateFactory.getInstance(type)
                        .generateCertificate(new ByteArrayInputStream(enc));
                } catch (CertificateException cee) {
                    throw ((IOException)new IOException(
                        Messages.getString("security.32")).initCause(cee)); //$NON-NLS-1$
                }
            }
        }
    }
}
