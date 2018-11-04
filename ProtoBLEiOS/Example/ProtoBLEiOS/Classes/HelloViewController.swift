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
import CoreBluetooth
import ProtoBLEiOS

class HelloViewController: UIViewController, HelloWorldDelegate {
    @IBOutlet weak var salutationText: UITextField!
    @IBOutlet weak var nameText: UITextField!
    @IBOutlet weak var writeButton: UIButton!
    @IBOutlet weak var responseLabel: UILabel!
    
    var centralManager: ProtoBLECentralManager!
    var helloBleService: HelloWorldBleService!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        centralManager = ProtoBLECentralManager.shared
        helloBleService = HelloWorldBleService(delegate: self)
    }
    
    var device : DeviceInformation? {
        didSet {
            centralManager.connectedDevice = device
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        centralManager.connect(helloBleService.bleService)

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func deviceSelected(_ newDevice: DeviceInformation) {
        if device != nil && device?.peripheral != nil {
            print("Disconnecting from \(device!.peripheral.name!)")
            device!.peripheral.delegate = nil
            nameText.isEnabled = false
            salutationText.isEnabled = false
            writeButton.isEnabled = false
        }
        print ("Device Selected " + newDevice.peripheral.name!)
        device = newDevice
    }
    
    func refreshUi() {
        print ("refreshUI --------")
        loadViewIfNeeded()
        
    }
    
    @IBAction func onWriteButton(_ sender: Any) {
        let intro = Introduction.with {
            $0.name = nameText.text!
            $0.salutation = salutationText!.text!
        }
        do {
            try helloBleService.helloWorld(introduction: intro)
        }
        catch {
            print ("Error!: \(error)")
        }
    }
    
    // MARK: - HelloBleServiceDelegate
    func helloWorldDidComplete(_ greeting: Greeting) {
        print ("onHelloWorld \(greeting.greeting)")
        responseLabel.text = greeting.greeting
        responseLabel.isEnabled = true
    }
    
    func helloWorldMessageProgress(current: Int, total: Int) {
        print("helloWorldProgress \(current) / \(total)")
    }
    
    func getTimeDidComplete(_ timeResponse: TimeResponse) {
        print ("onGetTime \(timeResponse.formattedTime)")
    }
    
    func getTimeMessageProgress(current: Int, total: Int) {
        print("getTimeMessageProgress \(current) / \(total)")
    }
    
    func onBleConnected() {
        print("HelloBleService: onBleConnected")
        writeButton.isEnabled = true
    }
    
    func onNoMessageAvailable() {
        print("HelloBleService: onNoMessageAvailable")
    }
    
    func bleDidDiscoverCharacteristics() {
        print("ProtoBleService Connected")
        writeButton.isEnabled = true
    }
    
    func bleDidError(_ error: Error) {
        print("Protoble Error: \(error)")
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
