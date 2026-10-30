import { marauroa } from "marauroa";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";

interface InspectSection {id:string;title:string;columns:string[];rows:string[][];details:string[];}
interface InspectEvent {payload:string;request:string;phase:string;window_id:string;}

/** Read-only inspection; every server string is inserted as text, never HTML. */
export class AdminInspectDialog extends DialogContentComponent {
    private static active?:AdminInspectDialog;
    private readonly navigation=document.createElement("div");
    private readonly heading=document.createElement("div");
    private readonly search=document.createElement("input");
    private readonly table=document.createElement("table");
    private readonly details=document.createElement("textarea");
    private readonly count=document.createElement("span");
    private readonly copy:HTMLButtonElement;
    private sections:InspectSection[]=[];
    private selected="";
    private windowId="";
    private request:{[key:string]:any}={};
    private order?:{column:number;descending:boolean};

    private constructor(){
        super("empty-div-template");this.componentElement.classList.add("admin-inspect-dialog");
        this.navigation.className="admin-inspect-navigation";
        const body=document.createElement("div");body.className="admin-inspect-body";
        const layout=document.createElement("div");layout.className="admin-inspect-layout";
        layout.append(this.navigation,body);this.componentElement.appendChild(layout);
        this.heading.className="admin-inspect-heading";
        const scroll=document.createElement("div");scroll.className="admin-inspect-table";scroll.appendChild(this.table);
        this.details.readOnly=true;this.details.className="admin-inspect-details";this.details.setAttribute("aria-label","Szczegóły wybranego wiersza");
        const filter=document.createElement("label");filter.className="admin-inspect-filter";filter.appendChild(document.createTextNode("Szukaj:"));
        this.search.type="search";this.search.setAttribute("aria-label","Szukaj w danych inspect");
        this.search.addEventListener("input",()=>this.drawRows());filter.append(this.search,this.count);
        body.append(this.heading,scroll,this.details,filter);
        this.copy=this.addButton("Kopiuj wiersz",()=>{if(this.details.value)navigator.clipboard?.writeText(this.details.value);});this.copy.disabled=true;
        this.addButton("Odśwież",()=>{
            const action:{[key:string]:any}={...this.request,inspection_window:this.windowId};
            if(Array.isArray(action.target_path))action.target_path="["+action.target_path.join("\t")+"]";
            marauroa.clientFramework.sendAction(action);
        });
        this.addButton("Zamknij",()=>this.frame?.close());
    }
    public static show(data:InspectEvent):void{
        if(data.phase==="refresh"&&(!this.active||this.active.windowId!==data.window_id))return;
        let snapshot:any,request:any;
        try{snapshot=JSON.parse(data.payload);request=JSON.parse(data.request);}catch{return;}
        if(!Array.isArray(snapshot.sections))return;
        if(!this.active){const dialog=new AdminInspectDialog();const frame=new FloatingWindow("Inspect",dialog,32,32);frame.setId("admin-inspect");dialog.setFrame(frame);this.active=dialog;}
        const dialog=this.active;
        if(dialog.windowId!==data.window_id){dialog.search.value="";dialog.selected="";dialog.order=undefined;}
        dialog.windowId=data.window_id;dialog.request=request;dialog.sections=snapshot.sections;
        dialog.frame?.setTitle("Inspect · "+String(snapshot.title));
        if(!dialog.sections.some(section=>section.id===dialog.selected))dialog.selected=dialog.sections[0]?.id||"";
        dialog.drawNavigation();dialog.drawRows();
    }
    public static closeOnLogout():void {this.active?.frame?.close();}
    public override onParentClose():void{if(AdminInspectDialog.active===this)AdminInspectDialog.active=undefined;}
    private drawNavigation():void{
        this.navigation.replaceChildren();
        for(const section of this.sections){const button=document.createElement("button");button.className="dialogbutton";button.textContent=section.title;
            button.classList.toggle("active",section.id===this.selected);button.setAttribute("aria-pressed",String(section.id===this.selected));
            button.onclick=()=>{this.selected=section.id;this.order=undefined;this.drawNavigation();this.drawRows();};this.navigation.appendChild(button);}
    }
    private drawRows():void{
        this.table.replaceChildren();this.details.value="";this.copy.disabled=true;
        const section=this.sections.find(section=>section.id===this.selected);if(!section)return;
        this.heading.textContent=section.title;
        const header=document.createElement("thead"),head=document.createElement("tr");
        section.columns.forEach((name,column)=>{const cell=document.createElement("th"),button=document.createElement("button");button.textContent=name;button.type="button";
            button.onclick=()=>{this.order={column,descending:this.order?.column===column?!this.order.descending:false};this.drawRows();};cell.appendChild(button);head.appendChild(cell);});header.appendChild(head);this.table.appendChild(header);
        const term=this.search.value.trim().toLocaleLowerCase("pl");
        const rows=section.rows.map((row,index)=>({row,index})).filter(({row})=>row.some(value=>String(value).toLocaleLowerCase("pl").includes(term)));
        if(this.order){const {column,descending}=this.order;rows.sort((a,b)=>String(a.row[column]).localeCompare(String(b.row[column]),"pl",{numeric:true})*(descending?-1:1));}
        const body=document.createElement("tbody");
        for(const {row,index} of rows){const tr=document.createElement("tr");tr.tabIndex=0;
            row.forEach(value=>{const cell=document.createElement("td");cell.textContent=String(value??"");tr.appendChild(cell);});
            const select=()=>{body.querySelectorAll(".selected").forEach(element=>element.classList.remove("selected"));tr.classList.add("selected");
                this.details.value=section.details?.[index]||row.map((value,column)=>section.columns[column]+": "+value).join("\n");this.copy.disabled=false;};
            tr.onclick=select;tr.onkeydown=e=>{if(e.key==="Enter"||e.key===" "){e.preventDefault();select();}};body.appendChild(tr);}
        this.table.appendChild(body);this.count.textContent=rows.length+" / "+section.rows.length;
    }
}
