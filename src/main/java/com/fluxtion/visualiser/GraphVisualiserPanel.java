/* 
 * Copyright (C) 2017 V12 Technology Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxtion.visualiser;

import com.mxgraph.io.mxGraphMlCodec;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Loads, displays and exports to PNG graphml files generated by Fluxtion.
 *
 *
 * @author Greg Higgins (greg.higgins@V12technology.com)
 */
//public class GraphVisualiserPanel extends JScrollPane {
public class GraphVisualiserPanel extends JPanel {

    private mxGraph graph;
    private mxGraphComponent graphComponent;
    private ArrayList<mxICell> highlightedCells;
    private ArrayList<mxICell> selectedCells;
    private mxHierarchicalLayout layoutImpl;

    public GraphVisualiserPanel() {
        setOpaque(true);
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

//        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "onZoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "onZoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "resetZoom");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "filteredView");

        am.put("onZoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphComponent.zoomIn();
            }
        });

        am.put("onZoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphComponent.zoomOut();
            }
        });

        am.put("resetZoom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                graphComponent.zoomActual();
                graphComponent.zoomAndCenter();
            }
        });

        am.put("filteredView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                foldCells(true);
            }
        });
    }

    public void addReloadAction(Action a) {
        InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "reload");
        am.put("reload", a);
    }

    private List<mxICell> addParents(mxICell cell, boolean recurse, List<mxICell> cells) {
        for (Object o : graph.getIncomingEdges(cell)) {
            mxCell edge = (mxCell) o;
            mxICell source = edge.getSource();
            cells.add(source);
            cells.add(edge);
            if (recurse) {
                addParents(source, recurse, cells);
            }
        }
        return cells;
    }

    private List<mxICell> addChildren(mxICell cell, boolean recurse, List<mxICell> cells) {
        for (Object o : graph.getOutgoingEdges(cell)) {
            mxCell edge = (mxCell) o;
            mxICell target = edge.getTarget();
            cells.add(target);
            cells.add(edge);
            if (recurse) {
                addChildren(target, recurse, cells);
            }
        }
        return cells;
    }

    public void exportPng(File pngFile) {
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
        if (pngFile.getParentFile().mkdirs()) {
            System.out.println("made directory");
        } else {
            System.out.println("FAILED to make directory");
        }
        try {
            ImageIO.write(image, "PNG", pngFile);
        } catch (IOException ex) {
            Logger.getLogger(GraphVisualiserPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void load(File f) {
        loadMxGraph(f);
        configureDisplay();
    }

    public void selectCellsBySearchString(String id) {
        Object[] allCells = mxGraphModel.getChildren(graph.getModel(), graph.getDefaultParent());
        List<mxICell> matchingCells = Arrays.stream(allCells).map((t) -> (mxICell) t).filter((t) -> {
            return t.getId().contains(id);
        }).collect(Collectors.toList());
        highlightCells(matchingCells, true);
    }

    public List<String> selectedIds() {
        List<String> ids = selectedCells.stream().map((m) -> m.getId()).collect(Collectors.toList());
        return ids;
    }

    public void highlightCellOnly(String cellId) {
        Object[] allCells = mxGraphModel.getChildren(graph.getModel(), graph.getDefaultParent());
        List<mxICell> matchingCells = Arrays.stream(allCells).map((t) -> (mxICell) t).filter((t) -> {
            return cellId.equals(t.getId());
        }).collect(Collectors.toList());
        selectedCells.clear();
        if (!matchingCells.isEmpty()) {
            highlightCells(matchingCells, false);
            graphComponent.scrollCellToVisible(matchingCells.get(0), false);
        }
    }

    public void selectCellsById(List<String> idList) {
        Object[] allCells = mxGraphModel.getChildren(graph.getModel(), graph.getDefaultParent());
        List<mxICell> matchingCells = Arrays.stream(allCells).map((t) -> (mxICell) t).filter((t) -> {
            return idList.contains(t.getId());
        }).collect(Collectors.toList());
        highlightCells(matchingCells, true);
    }

    private void highlightCells(List<mxICell> matchingCells, boolean highlightRelations) {
        //grey everything
        Object[] allCells = mxGraphModel.getChildren(graph.getModel(), graph.getDefaultParent());
        graph.setCellStyles(mxConstants.STYLE_OPACITY, "10", allCells);
        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "grey", allCells);
        //
        for (mxICell selectedCell : matchingCells) {
            if (highlightRelations) {
                addParents(selectedCell, true, highlightedCells);
                addChildren(selectedCell, true, highlightedCells);
            }
            selectedCells.add(selectedCell);
        }
        //highlight path nodes
        graph.setCellStyles(mxConstants.STYLE_OPACITY, "100", highlightedCells.toArray());
        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", highlightedCells.toArray());
        //red text for selected
        graph.setCellStyles(mxConstants.STYLE_OPACITY, "100", selectedCells.toArray());
        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "red", selectedCells.toArray());
    }

    public void zoom(boolean zoomIn) {
        if (zoomIn) {
            graphComponent.zoomIn();
        } else {
            graphComponent.zoomOut();
        }
    }

    public void fillterByName(String filter) {
        System.out.println("new filter:" + filter);
    }

    public mxGraph loadMxGraph(File f) {
        graph = new mxGraph();

        Map<String, Object> styleDefault = graph.getStylesheet().getDefaultEdgeStyle();
        styleDefault.put(mxConstants.STYLE_EDGE, mxEdgeStyle.SideToSide);
        styleDefault.put(mxConstants.STYLE_DASHED, true);

        //style
        mxStylesheet stylesheet = graph.getStylesheet();
        Map<String, Object> style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_OPACITY, 100);
        style.put(mxConstants.STYLE_FILLCOLOR, "#53B9F0");
        style.put(mxConstants.STYLE_FONTCOLOR, "black");
        style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
//        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        style.put(mxConstants.STYLE_FONTFAMILY, "Segoe");
        stylesheet.putCellStyle("EVENTHANDLER", style);

        style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_OPACITY, 100);
        style.put(mxConstants.STYLE_FILLCOLOR, "#ffbf80");
        style.put(mxConstants.STYLE_FONTCOLOR, "black");
        style.put(mxConstants.STYLE_FONTFAMILY, "Segoe");
        style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        style.put(mxConstants.STYLE_AUTOSIZE, 1);
        stylesheet.putCellStyle("EVENT", style);

        style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_OPACITY, 100);
        mxConstants.SPLIT_WORDS = false;
        style.put(mxConstants.STYLE_FONTCOLOR, "black");
//        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        style.put(mxConstants.STYLE_FILLCOLOR, "#53c68c");
        style.put(mxConstants.STYLE_FONTFAMILY, "Segoe");
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        style.put(mxConstants.STYLE_AUTOSIZE, 1);
        stylesheet.putCellStyle("NODE", style);

        style = new HashMap<>();
        style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CLOUD);
        style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
        style.put(mxConstants.STYLE_ROUNDED, true);
        style.put(mxConstants.STYLE_OPACITY, 100);
        mxConstants.SPLIT_WORDS = false;
        style.put(mxConstants.STYLE_FONTCOLOR, "red");
//        style.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        style.put(mxConstants.STYLE_FILLCOLOR, "#346789");
        style.put(mxConstants.STYLE_FONTFAMILY, "Segoe");
        style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
        style.put(mxConstants.STYLE_AUTOSIZE, 1);
        stylesheet.putCellStyle("SELECTED", style);

        try {
            Document document = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(f);
            Object parent = graph.getDefaultParent();
            mxGraphMlCodec.decode(document, graph);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(GraphVisualiserPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return graph;
    }

    private void configureDisplay() {
        highlightedCells = new ArrayList<>();
        selectedCells = new ArrayList<>();
        setOpaque(true);
        setLayout(new BorderLayout());
        graph.setCellsEditable(false);
        graph.setCellsDisconnectable(false);
        graph.setCellsLocked(false);
        graph.setConnectableEdges(false);
        graph.setAutoSizeCells(true);

        graph.setCellsMovable(true);
        graph.setCellsResizable(true);
        graph.setLabelsClipped(true);
        Object parent = graph.getDefaultParent();

        layoutImpl = new mxHierarchicalLayout(graph);
        layoutImpl.setInterRankCellSpacing(80);
        layoutImpl.setIntraCellSpacing(70);
        layoutImpl.setFineTuning(true);
        layoutImpl.execute(parent);

        //swing component
        graphComponent = new mxGraphComponent(graph);
        graphComponent.getViewport().setOpaque(true);
        graphComponent.getViewport().setBackground(Color.WHITE);
        graphComponent.getVerticalScrollBar().setUnitIncrement(20);
        graphComponent.updateComponents();
        graphComponent.zoomAndCenter();
        //click handler

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e1) {
                graph.getModel().beginUpdate();
                if (e1.getButton() == 1) {
                    if (!e1.isControlDown()) {
                        highlightedCells.clear();
                        selectedCells.clear();
                    }
                    final mxCell selectedCell = (mxCell) graphComponent.getCellAt(e1.getX(), e1.getY());
                    Object[] allCells = mxGraphModel.getChildren(graph.getModel(), graph.getDefaultParent());
                    if (selectedCell != null) {
                        final List<String> selectedIds = selectedIds();
                        if (selectedIds.contains(selectedCell.getId())) {
                            selectedIds.remove(selectedCell.getId());
                            highlightedCells.clear();
                            selectedCells.clear();
                            selectCellsById(selectedIds);
                        } else {
                            //set all grey
                            graph.setCellStyles(mxConstants.STYLE_OPACITY, "10", allCells);
                            graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "grey", allCells);
                            //find all on path
                            boolean directsOnly = e1.getClickCount() == 2;
                            List<mxICell> cells = addParents(selectedCell, !directsOnly, highlightedCells);
                            mxICell[] cellsArray = addChildren(selectedCell, !directsOnly, cells).toArray(new mxICell[cells.size()]);
                            //highlight path nodes
                            graph.setCellStyles(mxConstants.STYLE_OPACITY, "100", cellsArray);
                            graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", cellsArray);
                            //red text for selected
                            selectedCells.add(selectedCell);
                            graph.setCellStyles(mxConstants.STYLE_OPACITY, "100", selectedCells.toArray());
                            graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "red", selectedCells.toArray());
                        }
                    } else {
                        graph.setCellStyles(mxConstants.STYLE_OPACITY, "100", allCells);
                        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "black", allCells);
                        highlightedCells.clear();
                        selectedCells.clear();
                    }
                }
                graph.getModel().endUpdate();
                invalidate();
                repaint();
            }
        });
        add(graphComponent, BorderLayout.CENTER);
        graphComponent.setCenterPage(true);
    }

    public void foldCells(boolean fold) {
        graph.foldCells(fold);
        Object[] allCells = mxGraphModel.getChildren(graph.getModel(), graph.getDefaultParent());
        if (fold) {
            ArrayList list = new ArrayList();
            for (Object cell : allCells) {
                if (highlightedCells.contains(cell) || selectedCells.contains(cell)) {
                } else {
                    list.add(cell);
                }
            }
            allCells = list.toArray(new Object[list.size()]);
            graph.cellsRemoved(allCells);
            layoutImpl.setFineTuning(true);
            layoutImpl.execute(graph.getDefaultParent());
        }
    }

}
