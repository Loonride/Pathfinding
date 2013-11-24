/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.ui;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.logic.behavior.BehaviorNodeComponent;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.TreeAccessor;

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * @author synopia
 */
public class RenderableNode implements Component, TreeAccessor<RenderableNode> {
    private final List<RenderableNode> children = Lists.newArrayList();
    private transient PortList portList;

    private Node node;
    private Vector2f position;
    private Vector2f size;
    private transient TreeAccessor<RenderableNode> withoutModel;
    private transient TreeAccessor<RenderableNode> withModel;
    private transient BehaviorNodeComponent data;

    public RenderableNode() {
        this(null);
    }

    public RenderableNode(BehaviorNodeComponent data) {
        this.data = data;
        position = new Vector2f();
        size = new Vector2f(10, 5);
        portList = new PortList(this);
        withoutModel = new ChainedTreeAccessor<>(this, portList);
        withModel = new ChainedTreeAccessor<>(this, portList, new NodeTreeAccessor());
    }

    public void update() {
        List<RenderableNode> all = Lists.newArrayList(children);
        children.clear();
        for (RenderableNode renderableNode : all) {
            withoutModel.insertChild(-1, renderableNode);
        }
    }

    public TreeAccessor<RenderableNode> withoutModel() {
        return withoutModel;
    }

    public TreeAccessor<RenderableNode> withModel() {
        return withModel;
    }

    public PortList getPortList() {
        return portList;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Port getHoveredPort(float worldX, float worldY) {
        if (getInputPort().contains(worldX, worldY)) {
            return getInputPort();
        }
        for (Port port : getPorts()) {
            if (port.contains(worldX, worldY)) {
                return port;
            }
        }
        return null;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setPosition(float x, float y) {
        position = new Vector2f(x, y);
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public Node getNode() {
        return node;
    }

    public BehaviorNodeComponent getData() {
        return data;
    }

    public Port.InputPort getInputPort() {
        return getPortList().getInputPort();
    }

    public Iterable<Port> getPorts() {
        return getPortList().ports();
    }

    public void insertChild(int index, RenderableNode child) {
        if (index == -1) {
            children.add(child);
        } else {
            children.add(index, child);
        }
    }

    public void setChild(int index, RenderableNode child) {
        if (children.size() == index) {
            children.add(null);
        }
        if (children.get(index) != null) {
            Port.InputPort inputPort = children.get(index).getInputPort();
            inputPort.setTarget(null);
        }
        children.set(index, child);
    }

    public RenderableNode removeChild(int index) {
        RenderableNode remove = children.remove(index);
        remove.getInputPort().setTarget(null);
        return remove;
    }

    public RenderableNode getChild(int index) {
        if (children.size() > index) {
            return children.get(index);
        }
        return null;
    }

    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public int getMaxChildren() {
        return getNode().getMaxChildren();
    }

    public Vector2f getPosition() {
        Vector2f pos;
        RenderableNode targetNode = getInputPort().getTargetNode();
        if (targetNode != null) {
            pos = targetNode.getPosition();
        } else {
            pos = new Vector2f();
        }
        pos.add(position);
        return pos;
    }

    public Vector2f getSize() {
        return size;
    }

    @Override
    public String toString() {
        return getNode() != null ? getNode().toString() : "";
    }

    public boolean contains(float worldX, float worldY) {
        Vector2f p = getPosition();
        return p.x <= worldX && p.y <= worldY && worldX <= p.x + size.x && worldY <= p.y + size.y;
    }

    public void visit(Visitor visitor) {
        visitor.visit(this);
        for (RenderableNode child : children) {
            child.visit(visitor);
        }
    }

    public interface Visitor {
        void visit(RenderableNode node);
    }

    public class NodeTreeAccessor implements TreeAccessor<RenderableNode> {
        @Override
        public void insertChild(int index, RenderableNode child) {
            getNode().insertChild(index, child.getNode());
        }

        @Override
        public void setChild(int index, RenderableNode child) {
            getNode().setChild(index, child.getNode());
        }

        @Override
        public RenderableNode removeChild(int index) {
            getNode().removeChild(index);
            return null;
        }

        @Override
        public RenderableNode getChild(int index) {
            return null;
        }

        @Override
        public int getChildrenCount() {
            return getNode().getChildrenCount();
        }

        @Override
        public int getMaxChildren() {
            return getNode().getMaxChildren();
        }
    }
}