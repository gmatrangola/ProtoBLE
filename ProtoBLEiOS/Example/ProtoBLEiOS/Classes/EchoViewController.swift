// ProtoBLE - Protobuf RPC over Bluetooth Low Energy
// Copyright (c) 2018. ElectraZoom.com
//
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, version 3 of the License.
//
//     This program is distributed in the hope that it will be useful,
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//     GNU General Public License for more details.
//
//     You should have received a copy of the GNU General Public License
//     along with this program.  If not, see <https://www.gnu.org/licenses/>
//
//     This program is also available under a commercial license. If you wish
//     to redistribute this library and derivative work for commercial purposes
//     please see ProtoBLE.com to obtain a proprietary license that will fit
//     your needs.

import UIKit

class EchoViewController: UIViewController {

    @IBOutlet weak var startButton: UIButton!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var sentNumLabel: UILabel!
    @IBOutlet weak var sentTextLabel: UILabel!
    @IBOutlet weak var receivedNumLabel: UILabel!
    @IBOutlet weak var receivedText: UILabel!
    @IBOutlet weak var readText: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func onStartButton(_ sender: Any) {
    }
    
    @IBAction func onReadButton(_ sender: Any) {
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
