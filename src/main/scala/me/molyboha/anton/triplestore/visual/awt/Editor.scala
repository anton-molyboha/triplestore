package me.molyboha.anton.triplestore.visual.awt

import java.awt.event.{ActionEvent, ActionListener}
import java.awt._

import me.molyboha.anton.triplestore.data.model
import me.molyboha.anton.triplestore.data.model.Notion
import me.molyboha.anton.triplestore.visual.awt.layout.{SpringAutoLayout, SpringAutoLayout2}

class Editor(factory: model.Factory[String], startingNotion: Notion[String], viewKind: Editor.ViewKind, radius: Int = 2) extends Panel(new BorderLayout()) {
  class NotionAdder extends Panel {
    val editBox = new TextField(20)
    val button = new Button("New")
    add(editBox)
    add(button)
    button.addActionListener((actionEvent: ActionEvent) => {
      val notion = factory.notion(editBox.getText)
      Editor.this.view.pin(Editor.this.view.center)
      Editor.this.view.center = notion
      Editor.this.view.pin(notion)
    })
  }
  class RelationAdder extends Panel(new GridLayout(2, 4)) {
    val setSubject = new Button("Subject")
    val setVerb = new Button("Verb")
    val setObject = new Button("Object")
    val createButton = new Button("Create")
    val emptyText = "<empty>"
    val subjectLabel = new Label(emptyText)
    val verbLabel = new Label(emptyText)
    val objectLabel = new Label(emptyText)
    val dataText = new TextField()
    createButton.setEnabled(false)

    private var _subject: Option[Notion[String]] = None
    private var _verb: Option[Notion[String]] = None
    private var _obj: Option[Notion[String]] = None
    def subject: Option[Notion[String]] = _subject
    def verb: Option[Notion[String]] = _verb
    def obj: Option[Notion[String]] = _obj
    def subject_=(v: Option[Notion[String]]): Unit = {
      _subject = v
      subjectLabel.setText(v.map(_.toString).getOrElse(emptyText))
      createButton.setEnabled(_subject.isDefined && _verb.isDefined && _obj.isDefined)
    }
    def verb_=(v: Option[Notion[String]]): Unit = {
      _verb = v
      verbLabel.setText(v.map(_.toString).getOrElse(emptyText))
      createButton.setEnabled(_subject.isDefined && _verb.isDefined && _obj.isDefined)
    }
    def obj_=(v: Option[Notion[String]]): Unit = {
      _obj = v
      objectLabel.setText(v.map(_.toString).getOrElse(emptyText))
      createButton.setEnabled(_subject.isDefined && _verb.isDefined && _obj.isDefined)
    }

    setSubject.addActionListener((e: ActionEvent) => {subject = Some(Editor.this.view.center)})
    setVerb.addActionListener((e: ActionEvent) => {verb = Some(Editor.this.view.center)})
    setObject.addActionListener((e: ActionEvent) => {obj = Some(Editor.this.view.center)})
    createButton.addActionListener((e: ActionEvent) => {
      val relName = dataText.getText
      val relData = if( relName.isEmpty ) None else Some(relName)
      val rel = factory.relation(subject.get, verb.get, obj.get, relData)
      subject = None
      verb = None
      obj = None
      Editor.this.view.updateLayout()
    })

    add(setSubject)
    add(setVerb)
    add(setObject)
    add(createButton)
    add(subjectLabel)
    add(verbLabel)
    add(objectLabel)
    add(dataText)
  }

  val (graphView, layoutFun) = viewKind.createView
  val view = new CentralView[String](graphView, startingNotion, radius, layoutFun)
  val notionAdder = new NotionAdder
  val relationAdder = new RelationAdder

  val topPanel = new Panel
  topPanel.add(notionAdder)
  topPanel.add(relationAdder)
  add(graphView)
  add(topPanel, BorderLayout.NORTH)
}

object Editor {
  trait ViewKind {
    def createView: (GraphViewBase[String], (Iterable[Notion[String]]) => Unit)
  }
  object SimpleView extends ViewKind {
    override def createView: (GraphViewBase[String], (Iterable[Notion[String]]) => Unit) = {
      val res = new GraphView[String]
      (res, SpringAutoLayout(_, res))
    }
  }
  object FullView extends ViewKind {
    override def createView: (GraphViewBase[String], (Iterable[Notion[String]]) => Unit) = {
      val res = new GraphView2[String]
      (res, SpringAutoLayout2(_, res))
    }
  }
}
