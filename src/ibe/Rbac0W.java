package ibe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import base.Action;
import base.ActorMachine;
import base.InvalidMeasureException;
import base.Property;
import base.SimLogger.Log;
import base.Simulation;
import base.Workflow;
import base.WorkloadScheme;
import base.WorkloadState;

public class Rbac0W extends WorkloadScheme {

    public Rbac0W(WorkloadState initState) {
        super(initState);
    }

    @Override
    public boolean isMeasurable(Property p) {
        if(super.isMeasurable(p)) return true;
        Property qTest = new Property(this, "__test", new String[0]);
        if(!qTest.matchQualifier(p)) {
            return false;
        }
        if(p.name.equals(Q_ACTIONS)) {
            return true;
        }
        return false;
    }

    @Override
    public Set<Property> getMeasurables() {
        HashSet<Property> ret = new HashSet<Property>();
        ret.addAll(super.getMeasurables());
        ret.add(new Property(this, Q_ACTIONS, new String[0]));
        return ret;
    }

    @Override
    public Object getCurMeasure(Property p) {
        try {
            if(super.isMeasurable(p)) {
                return super.getCurMeasure(p);
            }
        } catch(InvalidMeasureException e) {
            e.printStackTrace();
        }
        Property qTest = new Property(this, "__test", new String[0]);
        if(!qTest.matchQualifier(p)) {
            throw new InvalidMeasureException(p);
        }
        switch(p.name) {
            case WorkloadScheme.Q_ACTIONS:
                return AVAIL_ACTIONS;
        }
        throw new InvalidMeasureException(p);
    }

    private void command(Action acAction) {
        Rbac0WState rState = (Rbac0WState) state;
        switch(acAction.name) {
            case "addU":
                // This assumes we can only add normal users!!
                String name = acAction.params[1];
                ActorMachine am = new ActorUser();
                am.actor = name;
                am.actorType = "ibe.ActorUser";
                am.sys = sys;
                sys.addActor(name, am);
                rState.addUser(name);
                break;
            case "delU":
                name = acAction.params[1];
                sys.removeActor(name);
                rState.delUser(name);
                break;
            case "addR":
                rState.addRole(acAction.params[1]);
                break;
            case "delR":
                rState.delRole(acAction.params[1]);
                break;
            case "addP":
                rState.addPermission(acAction.params[1]);
                break;
            case "delP":
                rState.delPermission(acAction.params[1]);
                break;
            case "assignUser":
                rState.assignUser(acAction.params[1], acAction.params[2]);
                break;
            case "revokeUser":
                rState.revokeUser(acAction.params[1], acAction.params[2]);
                break;
            case "assignPermission":
                rState.assignPermission(acAction.params[1], acAction.params[2]);
                break;
            case "revokePermission":
                rState.revokePermission(acAction.params[1], acAction.params[2]);
                break;
            default:
                throw new RuntimeException("Unrecognized command: " + acAction);
        }
    }

    private void query(Action acAction) {
        Rbac0WState rState = (Rbac0WState) state;
        String u, r, p;
        switch(acAction.name) {
            case "UR": {
                u = acAction.params[1];
                r = acAction.params[2];
                if(rState.U.contains(u)) {
                    if(rState.R.contains(r)) {
                        // For logging purposes, this is a different condition: we're not sure u is
                        // in UA even if it's in U
                        if(rState.UA.containsKey(u)) {
                            lastQueryResult = rState.UA.get(u).contains(r);
                        } else {
                            lastQueryResult = false;
                        }
                    } else {
                        Log.d("Query mismatch: UR with non-existent role " + r);
                        lastQueryResult = false;
                    }
                } else {
                    Log.d("Query mismatch: UR with non-existent user " + u);
                    lastQueryResult = false;
                }
                return;
            }
            case "PA": {
                // Note params: (r, p), which is opposite of storage order
                r = acAction.params[1];
                p = acAction.params[2];
                if(rState.P.contains(p)) {
                    if(rState.R.contains(r)) {
                        // For logging purposes, this is a different condition: we're not sure p is
                        // in PA even if it's in P
                        if(rState.PA.containsKey(p)) {
                            lastQueryResult = rState.PA.get(p).contains(r);
                        } else {
                            lastQueryResult = false;
                        }
                    } else {
                        Log.d("Query mismatch: PA with non-existent role " + r);
                        lastQueryResult = false;
                    }
                } else {
                    Log.d("Query mismatch: PA with non-existent permission " + p);
                    lastQueryResult = false;
                }
                return;
            }
            case "auth": {
                u = acAction.params[1];
                p = acAction.params[2];
                if(rState.U.contains(u)) {
                    if(rState.P.contains(p)) {
                        // For logging purposes, this is a different condition: we're not sure p is
                        // in PA or u is in UA
                        if(rState.UA.containsKey(u) && rState.PA.containsKey(p)) {
                            Set<String> uRoles = rState.UA.get(u);
                            Set<String> pRoles = rState.PA.get(p);
                            lastQueryResult = !Collections.disjoint(uRoles, pRoles);
                        } else {
                            lastQueryResult = false;
                        }
                    } else {
                        Log.d("Query mismatch: auth with non-existent permission " + p);
                        lastQueryResult = false;
                    }
                } else {
                    Log.d("Query mismatch: auth with non-existent user " + u);
                    lastQueryResult = false;
                }
                return;
            }
        }
    }

    @Override
    public void action(Action acAction) {
        super.action(acAction);
        if(!acAction.isQuery) {
            command(acAction);
        } else {
            query(acAction);
        }

    }


    public static final String[] AVAIL_ACTIONS = {
        "addU", "addR", "delR", "addP", "delP",
        "assignUser", "revokeUser", "assignPermission", "revokePermission",
        "UR", "PA", "auth"
    };


    private <T> T randomInSet(Set<T> set) {
        int size = set.size();
        int j = Simulation.rand.nextInt(size);
        int i = 0;
        for(T obj : set) {
            if(i == j) {
                return obj;
            }
            i++;
        }
        throw new RuntimeException("Something went terribly while picking random element of set");
    }

    @Override
    public Action addParameters(Action a, Workflow w) {
        Rbac0WState rState = (Rbac0WState) state;

        if(a.name.equals("addU")) {

            String user = a.params[1];
            // If user isn't specified, find the next available user by number
            if(user == null) {
                int u = 0;
                user = "u" + u;
                while(rState.U.contains(user)) {
                    u++;
                    user = "u" + u;
                }
            }
            return new Action("addU", a.params[0], user);

        } else if(a.name.equals("delU")) {

            String user = a.params[1];
            // If user isn't specified, find a random user
            if(user == null) {
                boolean userWorks = false;
                Set<String> users = new TreeSet<String>(rState.U);

                // Find a user that works
                while(!userWorks) {
                    // No user works
                    if(users.isEmpty()) return null;

                    user = randomInSet(users);
                    if(user.startsWith("u")) {
                        // Only delete regular users
                        userWorks = true;
                        break;
                    }
                    // user doesn't work
                    users.remove(user);
                }
            }
            return new Action("delU", a.params[0], user);

        } else if(a.name.equals("addP")) {

            String permission = a.params[1];
            // If permission isn't specified, find the next available permission by number
            if(permission == null) {
                int p = 0;
                permission = "p" + p;
                while(rState.P.contains(permission)) {
                    p++;
                    permission = "p" + p;
                }
            }
            return new Action("addP", a.params[0], permission);

        } else if(a.name.equals("delP")) {

            String permission = a.params[1];
            // If permission isn't specified, find a random permission
            if(permission == null) {
                // There is no permission
                if(rState.P.isEmpty()) return null;
                permission = randomInSet(rState.P);
            }
            return new Action("delP", a.params[0], permission);

        } else if(a.name.equals("addR")) {

            String role = a.params[1];
            // If role isn't specified, find the next available role by number
            if(role == null) {
                int r = 0;
                role = "r" + r;
                while(rState.R.contains(role)) {
                    r++;
                    role = "r" + r;
                }
            }
            return new Action("addR", a.params[0], role);

        } else if(a.name.equals("delR")) {

            String role = a.params[1];
            // If role isn't specified, find a random role
            if(role == null) {
                // There is no role
                if(rState.R.isEmpty()) return null;
                role = randomInSet(rState.R);
            }
            return new Action("delR", a.params[0], role);

        } else if(a.name.equals("assignUser")) {

            String user = a.params[1];
            String role = a.params[2];

            // If user isn't specified, find one that works
            if(user == null) {
                boolean userWorks = false;
                Set<String> users = new TreeSet<String>(rState.U);

                // Find a user that works
                while(!userWorks) {
                    // No user works
                    if(users.isEmpty()) return null;

                    user = randomInSet(users);
                    if(!rState.UA.containsKey(user)) {
                        // Since this user has no roles, it works
                        userWorks = true;
                        break;
                    } else if(role == null) {
                        // Since role isn't specified, just make sure at least one role can be
                        // assigned this user
                        Set<String> availableRoles = new HashSet<String>(rState.R);
                        // We know the user is in UA because of above condition
                        availableRoles.removeAll(rState.UA.get(user));
                        if(!availableRoles.isEmpty()) {
                            userWorks = true;
                            break;
                        }
                    } else {
                        // Make sure this user isn't already assigned to the specified role. We know
                        // the user is in UA because of above condition.
                        if(!rState.UA.get(user).contains(role)) {
                            userWorks = true;
                            break;
                        }
                    }
                    // user doesn't work
                    users.remove(user);
                }
            }

            // If role isn't specified, find one that works with the specified user
            if(role == null) {
                boolean roleWorks = false;
                Set<String> roles = new TreeSet<String>(rState.R);

                // Find a role that works
                while(!roleWorks) {
                    // No role works
                    if(roles.isEmpty()) return null;

                    role = randomInSet(roles);
                    // Make sure this role isn't already assigned the specified user
                    if(!rState.UA.containsKey(user) ||
                            !rState.UA.get(user).contains(role)) {
                        roleWorks = true;
                        break;
                    }
                    roles.remove(role);
                }
            }

            return new Action("assignUser", a.params[0], user, role);

        } else if(a.name.equals("revokeUser")) {

            String user = a.params[1];
            String role = a.params[2];

            // If user isn't specified, find one that works
            if(user == null) {
                boolean userWorks = false;
                Set<String> users = new TreeSet<String>(rState.U);

                // Find a user that works
                while(!userWorks) {
                    // No user works
                    if(users.isEmpty()) return null;

                    user = randomInSet(users);
                    if(!rState.UA.containsKey(user)) {
                        // Since this user has no roles, it doesn't work, so there's no reason
                        // to do anything
                    } else if(role == null) {
                        // Since role isn't specified, just make sure this user is assigned to
                        // at least one role. We know the user is in UA because of above
                        // condition.
                        Set<String> assignedRoles = new HashSet<String>(rState.UA.get(user));
                        // Make sure the roles exist, just in case
                        assignedRoles.retainAll(rState.R);
                        if(!assignedRoles.isEmpty()) {
                            userWorks = true;
                            break;
                        }
                    } else {
                        // Make sure this user is assigned to the specified role. We know the
                        // user is in UA because of above condition.
                        if(rState.UA.get(user).contains(role)) {
                            userWorks = true;
                            break;
                        }
                    }
                    // user doesn't work
                    users.remove(user);
                }
            }

            // If role isn't specified, find one that works with the specified user
            if(role == null) {
                boolean roleWorks = false;
                Set<String> roles = new TreeSet<String>(rState.R);

                // Find a role that works
                while(!roleWorks) {
                    // No role works
                    if(roles.isEmpty()) return null;

                    role = randomInSet(roles);
                    // Make sure this role is assigned the specified user
                    if(rState.UA.containsKey(user) &&
                            rState.UA.get(user).contains(role)) {
                        roleWorks = true;
                        break;
                    }
                    roles.remove(role);
                }
            }

            return new Action("revokeUser", a.params[0], user, role);

        } else if(a.name.equals("assignPermission")) {

            String permission = a.params[1];
            String role = a.params[2];

            // If permission isn't specified, find one that works
            if(permission == null) {
                boolean permWorks = false;
                Set<String> perms = new TreeSet<String>(rState.P);

                // Find a permission that works
                while(!permWorks) {
                    // No permission works
                    if(perms.isEmpty()) return null;

                    permission = randomInSet(perms);
                    if(!rState.PA.containsKey(permission)) {
                        // Since this permission has no roles, it works
                        permWorks = true;
                        break;
                    } else if(role == null) {
                        // Since role isn't specified, just make sure at least one role can be
                        // assigned this permission
                        Set<String> availableRoles = new HashSet<String>(rState.R);
                        // We know the permission is in PA because of above condition
                        availableRoles.removeAll(rState.PA.get(permission));
                        if(!availableRoles.isEmpty()) {
                            permWorks = true;
                            break;
                        }
                    } else {
                        // Make sure this permission isn't already assigned to the specified role.
                        // We know the permission is in PA because of above condition.
                        if(!rState.PA.get(permission).contains(role)) {
                            permWorks = true;
                            break;
                        }
                    }
                    // permission doesn't work
                    perms.remove(permission);
                }
            }

            // If role isn't specified, find one that works with the specified permission
            if(role == null) {
                boolean roleWorks = false;
                Set<String> roles = new TreeSet<String>(rState.R);

                // Find a role that works
                while(!roleWorks) {
                    // No role works
                    if(roles.isEmpty()) return null;

                    role = randomInSet(roles);
                    // Make sure this role isn't already assigned the specified permission
                    if(!rState.PA.containsKey(permission) ||
                            !rState.PA.get(permission).contains(role)) {
                        roleWorks = true;
                        break;
                    }
                    roles.remove(role);
                }
            }

            return new Action("assignPermission", a.params[0], permission, role);

        } else if(a.name.equals("revokePermission")) {

            String permission = a.params[1];
            String role = a.params[2];

            // If permission isn't specified, find one that works
            if(permission == null) {
                boolean permWorks = false;
                Set<String> perms = new TreeSet<String>(rState.P);

                // Find a permission that works
                while(!permWorks) {
                    // No permission works
                    if(perms.isEmpty()) return null;

                    permission = randomInSet(perms);
                    if(!rState.PA.containsKey(permission)) {
                        // Since this permission has no roles, it doesn't work, so there's no reason
                        // to do anything
                    } else if(role == null) {
                        // Since role isn't specified, just make sure this permission is assigned to
                        // at least one role. We know the permission is in PA because of above
                        // condition.
                        Set<String> assignedRoles = new HashSet<String>(rState.PA.get(permission));
                        // Make sure the roles exist, just in case
                        assignedRoles.retainAll(rState.R);
                        if(!assignedRoles.isEmpty()) {
                            permWorks = true;
                            break;
                        }
                    } else {
                        // Make sure this permission is assigned to the specified role. We know the
                        // permission is in PA because of above condition.
                        if(rState.PA.get(permission).contains(role)) {
                            permWorks = true;
                            break;
                        }
                    }
                    // permission doesn't work
                    perms.remove(permission);
                }
            }

            // If role isn't specified, find one that works with the specified permission
            if(role == null) {
                boolean roleWorks = false;
                Set<String> roles = new TreeSet<String>(rState.R);

                // Find a role that works
                while(!roleWorks) {
                    // No role works
                    if(roles.isEmpty()) return null;

                    role = randomInSet(roles);
                    // Make sure this role is assigned the specified permission
                    if(rState.PA.containsKey(permission) &&
                            rState.PA.get(permission).contains(role)) {
                        roleWorks = true;
                        break;
                    }
                    roles.remove(role);
                }
            }

            return new Action("revokePermission", a.params[0], permission, role);

        } else if(a.name.equals("auth")) {

            String actor = a.params[0];
            String user = a.params[1];
            String permission = a.params[2];

            if(user == null) {
                if(rState.U.contains(actor)) {
                    user = actor;
                } else if(rState.U.isEmpty()) {
                    throw new RuntimeException("No users exist? Who is running this??");
                } else {
                    Log.w("auth requested by non-existent user, filling in someone else...");
                    user = randomInSet(rState.U);
                }
            }
            if(permission == null) {
                boolean permWorks = false;
                Set<String> permissions = new TreeSet<String>(rState.P);

                if(!rState.UA.containsKey(user)) {
                    return null;
                }
                Set<String> uRoles = rState.UA.get(user);

                while(!permWorks) {
                    // No permission works
                    if(permissions.isEmpty()) return null;

                    permission = randomInSet(permissions);
                    if(rState.PA.containsKey(permission) &&
                            !Collections.disjoint(uRoles, rState.PA.get(permission))) {
                        permWorks = true;
                        break;
                    }
                    // permission doesn't work
                    permissions.remove(permission);
                }
            }

            return new Action(true, "auth", actor, user, permission);

        } else {

            // No explicit instructions for this action, just return it as-is
            Log.w("Rbac0W: No recipe to add parameters to action " + a);
            return a;

        }
    }
}
